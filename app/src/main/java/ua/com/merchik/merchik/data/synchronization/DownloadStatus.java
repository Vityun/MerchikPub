package ua.com.merchik.merchik.data.synchronization;


public enum DownloadStatus {
    SUCCESS(0), ERROR(1), PENDING(2);

    private final int code;

    DownloadStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DownloadStatus fromCode(int code) {
        for (DownloadStatus status : values()) {
            if (status.code == code) return status;
        }
        return PENDING;
    }
}

