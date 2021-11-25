package ua.com.merchik.merchik.data;

public class OptionsButtons {

    private long id;
    private int optionId;
    private int wpId;
    private long dad2;
    private String name;
    private String signal;

    public OptionsButtons(long id, int optionId, int wpId, String name, String signal){
        this.id = id;
        this.optionId = optionId;
        this.wpId = wpId;
        this.name = name;
        this.signal = signal;
    }

    public long getId() {
        return id;
    }

    public int getOptionId() {
        return optionId;
    }

    public int getWpId() {
        return wpId;
    }

    public String getName() {
        return name;
    }

    public String getSignal() {
        return signal;
    }

}
