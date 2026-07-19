package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.SHARE_IMAGE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonthlyReportShareImageService {

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1350;
    private static final int MAX_RENDERED_COCKTAILS = 3;
    private static final String DIRECTORY = "reports/monthly";
    private static final Color BACKGROUND = new Color(255, 248, 246);
    private static final Color TEXT = new Color(38, 34, 33);
    private static final Color MUTED_TEXT = new Color(112, 103, 100);
    private static final Color ACCENT = new Color(255, 99, 72);
    private static final Color TRACK = new Color(241, 226, 222);

    private final MonthlyReportService monthlyReportService;
    private final S3Client s3Client;
    private final S3Properties properties;

    public MonthlyReportShareImageResponse createShareImage(Long userId, int year, int month) {
        MonthlyReportResponse report = monthlyReportService.getMonthlyReport(userId, year, month);
        if (!StringUtils.hasText(properties.bucket())) {
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        }

        byte[] image = render(report);
        String objectKey = objectKey(report.year(), report.month(), image);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(objectKey)
                            .contentType("image/png")
                            .contentDisposition("inline; filename=\"moodtail-monthly-report.png\"")
                            .build(),
                    RequestBody.fromBytes(image)
            );
            String imageUrl = s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(properties.bucket())
                            .key(objectKey)
                            .build())
                    .toExternalForm();
            return new MonthlyReportShareImageResponse(imageUrl);
        } catch (SdkException exception) {
            log.error("Failed to upload monthly report share image", exception);
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        }
    }

    private byte[] render(MonthlyReportResponse report) {
        BufferedImage characterImage = loadCharacterImage(
                report.monthlyMoodType().characterImageUrl()
        );
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            configure(graphics);
            graphics.setColor(BACKGROUND);
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            drawHeader(graphics, report, characterImage);
            drawTasteProfile(graphics, report.displayAverageTasteScores());
            drawActivity(graphics, report.activity());
            drawCocktails(graphics, report);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", output)) {
                throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            log.error("Failed to render monthly report share image", exception);
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        } finally {
            graphics.dispose();
        }
    }

    private void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private BufferedImage loadCharacterImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        try {
            URI uri = URI.create(imageUrl);
            S3Uri s3Uri = s3Client.utilities().parseUri(uri);
            if (!properties.bucket().equals(s3Uri.bucket().orElse(null))
                    || s3Uri.key().isEmpty()
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || uri.getUserInfo() != null) {
                return null;
            }
            byte[] image = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(s3Uri.key().orElseThrow())
                            .build())
                    .asByteArray();
            return ImageIO.read(new ByteArrayInputStream(image));
        } catch (IllegalArgumentException | IOException | SdkException exception) {
            log.warn("Could not include the monthly mood character in the share image", exception);
            return null;
        }
    }

    private void drawHeader(
            Graphics2D graphics,
            MonthlyReportResponse report,
            BufferedImage characterImage
    ) {
        graphics.setColor(ACCENT);
        graphics.fill(new RoundRectangle2D.Double(70, 65, 940, 360, 48, 48));
        graphics.setColor(Color.WHITE);
        graphics.setFont(font(Font.BOLD, 42));
        graphics.drawString("MoodTail Monthly Report", 120, 135);
        graphics.setFont(font(Font.PLAIN, 30));
        graphics.drawString("%d. %02d".formatted(report.year(), report.month()), 120, 190);
        graphics.setFont(font(Font.BOLD, 64));
        int textWidth = characterImage == null ? 820 : 540;
        graphics.drawString(
                ellipsize(graphics, report.monthlyMoodType().name(), textWidth),
                120,
                295
        );
        graphics.setFont(font(Font.PLAIN, 28));
        graphics.drawString(report.monthlyMoodType().typeCode(), 120, 345);
        String description = report.monthlyMoodType().shortDescription();
        if (StringUtils.hasText(description)) {
            graphics.setFont(font(Font.PLAIN, 24));
            graphics.drawString(ellipsize(graphics, description, textWidth), 120, 390);
        }
        if (characterImage != null) {
            graphics.drawImage(characterImage, 730, 135, 230, 230, null);
        }
    }

    private void drawTasteProfile(
            Graphics2D graphics,
            MonthlyReportResponse.DisplayTasteScores scores
    ) {
        drawCard(graphics, 70, 465, 940, 385);
        graphics.setColor(TEXT);
        graphics.setFont(font(Font.BOLD, 34));
        graphics.drawString("Average Taste Profile", 115, 530);

        String[] labels = {"Alcohol", "Sweet", "Sour", "Fresh", "Bitter"};
        int[] values = {
                scores.alcoholIntensity(),
                scores.sweetness(),
                scores.sourness(),
                scores.refreshing(),
                scores.bitterness()
        };
        for (int index = 0; index < labels.length; index++) {
            int y = 590 + index * 52;
            graphics.setFont(font(Font.PLAIN, 23));
            graphics.setColor(MUTED_TEXT);
            graphics.drawString(labels[index], 115, y + 18);
            graphics.setColor(TRACK);
            graphics.fillRoundRect(260, y, 600, 20, 20, 20);
            graphics.setColor(ACCENT);
            graphics.fillRoundRect(260, y, values[index] * 6, 20, 20, 20);
            graphics.setColor(TEXT);
            graphics.drawString(String.valueOf(values[index]), 890, y + 18);
        }
    }

    private void drawActivity(Graphics2D graphics, MonthlyReportResponse.Activity activity) {
        drawCard(graphics, 70, 885, 450, 180);
        drawCard(graphics, 560, 885, 450, 180);
        drawMetric(graphics, 110, 935, "Mood Tests", activity.testCount());
        drawMetric(graphics, 600, 935, "Drink Records", activity.drinkingRecordCount());
    }

    private void drawMetric(Graphics2D graphics, int x, int y, String label, long value) {
        graphics.setColor(MUTED_TEXT);
        graphics.setFont(font(Font.PLAIN, 24));
        graphics.drawString(label, x, y);
        graphics.setColor(TEXT);
        graphics.setFont(font(Font.BOLD, 56));
        graphics.drawString(String.valueOf(value), x, y + 78);
    }

    private void drawCocktails(Graphics2D graphics, MonthlyReportResponse report) {
        drawCard(graphics, 70, 1100, 940, 180);
        graphics.setColor(TEXT);
        graphics.setFont(font(Font.BOLD, 30));
        graphics.drawString("Top Cocktails", 115, 1155);
        graphics.setFont(font(Font.PLAIN, 24));

        if (report.frequentCocktails().isEmpty()) {
            graphics.setColor(MUTED_TEXT);
            graphics.drawString("No drinking records this month", 115, 1220);
            return;
        }

        int x = 115;
        int renderedCocktailCount = Math.min(
                report.frequentCocktails().size(),
                MAX_RENDERED_COCKTAILS
        );
        for (int index = 0; index < renderedCocktailCount; index++) {
            MonthlyReportResponse.FrequentCocktail cocktail =
                    report.frequentCocktails().get(index);
            graphics.setColor(ACCENT);
            graphics.drawString(cocktail.ranking() + ".", x, 1213);
            graphics.setColor(TEXT);
            String name = StringUtils.hasText(cocktail.nameKo())
                    ? cocktail.nameKo()
                    : cocktail.nameEn();
            graphics.drawString(ellipsize(graphics, name, 210), x + 32, 1213);
            x += 295;
        }
    }

    private void drawCard(Graphics2D graphics, int x, int y, int width, int height) {
        graphics.setColor(Color.WHITE);
        graphics.fill(new RoundRectangle2D.Double(x, y, width, height, 36, 36));
    }

    private Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    private String ellipsize(Graphics2D graphics, String value, int maxWidth) {
        if (value == null) {
            return "";
        }
        FontMetrics metrics = graphics.getFontMetrics();
        if (metrics.stringWidth(value) <= maxWidth) {
            return value;
        }
        int end = value.length();
        while (end > 0 && metrics.stringWidth(value.substring(0, end) + "...") > maxWidth) {
            end--;
        }
        return value.substring(0, end) + "...";
    }

    private String objectKey(int year, int month, byte[] image) {
        return "%s/%d/%02d/%s.png".formatted(DIRECTORY, year, month, sha256(image));
    }

    private String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
