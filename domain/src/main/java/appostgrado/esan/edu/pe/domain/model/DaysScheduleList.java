package appostgrado.esan.edu.pe.domain.model;

public class DaysScheduleList {
    private String name;
    private boolean today;

    public DaysScheduleList(String name, boolean today){
        this.name = name;
        this.today = today;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }
}
