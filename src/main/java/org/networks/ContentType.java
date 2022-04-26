package org.networks;

public enum ContentType {
    PLAIN("text/plain", "txt"),
    HTML("text/html", "html"),
    CSS("text/css", "css"),
    JS("application/javascript", "js"),
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpeg"),
    SVG("image/svg+xml", "svg");

    private final String type;
    private final String extension;

    ContentType(String type, String extension) {
        this.type = type;
        this.extension = extension;
    }

    public static ContentType of(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        for (var value : ContentType.values()) {
            if (extension.equals(value.extension)) {
                return value;
            }
        }
        return PLAIN;
    }

    public String getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }
}
