package br.com.comunicaluno.fx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.InputStream;

public final class FxAssets {

    public static final String LOGO_FULL = "logo-full.png";
    public static final String LOGO_MARK = "logo-mark.png";
    public static final String AVATAR = "placeholder-avatar.png";
    public static final String POST_IMAGE = "placeholder-post-image.png";
    public static final String ATTACHMENT = "icon-attachment.png";
    public static final String EVENT_COVER = "placeholder-event-cover.png";
    public static final String DISCIPLINE_COVER = "placeholder-post-image.png";
    public static final String EMPTY_FEED = "placeholder-empty-feed.png";
    public static final String EMPTY_CHAT = "placeholder-empty-chat.png";
    public static final String NOTIFICATION = "placeholder-notification.png";
    public static final String ICON_ANNOUNCEMENT = "icon-announcement.png";
    public static final String ICON_ATTACHMENT = "icon-attachment.png";
    public static final String ICON_CALENDAR = "icon-calendar.png";
    public static final String ICON_COMMENT = "icon-comment.png";
    public static final String ICON_EXIT = "icon-exit.png";
    public static final String ICON_FEED = "icon-feed.png";
    public static final String ICON_HOME = "icon-home.png";
    public static final String ICON_IMAGE = "icon-image.png";
    public static final String ICON_LIKE_FILLED = "icon-like-filled.png";
    public static final String ICON_LIKE_OUTLINE = "icon-like-outline.png";
    public static final String ICON_PROFILE = "icon-profile.png";
    public static final String ICON_SHARE = "icon-share.png";

    private FxAssets() {
    }

    public static Image image(String assetName) {
        InputStream in = FxAssets.class.getClassLoader().getResourceAsStream("assets/" + assetName);
        if (in == null) {
            return null;
        }
        return new Image(in);
    }

    public static Image imageFromPathOrAsset(String path, String fallbackAsset) {
        if (path != null && !path.isBlank()) {
            File file = new File(path);
            if (file.exists() && file.isFile() && isImage(file.getName())) {
                return new Image(file.toURI().toString());
            }
        }
        return image(fallbackAsset);
    }

    public static ImageView view(String assetName, double width, double height) {
        return view(image(assetName), width, height);
    }

    public static ImageView view(Image image, double width, double height) {
        ImageView view = new ImageView(image);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    public static boolean isImage(String path) {
        if (path == null) {
            return false;
        }
        String lower = path.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp");
    }
}
