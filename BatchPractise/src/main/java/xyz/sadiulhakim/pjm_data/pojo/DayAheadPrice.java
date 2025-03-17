package xyz.sadiulhakim.pjm_data.pojo;

public class DayAheadPrice {
    private String datetime_beginning_ept;
    private String pnode_name;
    private double system_energy_price_da;
    private double total_lmp_da;
    private double congestion_price_da;
    private double marginal_loss_price_da;
    private double total_da;

    public DayAheadPrice() {
    }

    public DayAheadPrice(String datetime_beginning_ept, String pnode_name, double system_energy_price_da,
                         double total_lmp_da, double congestion_price_da, double marginal_loss_price_da,
                         double total_da) {
        this.datetime_beginning_ept = datetime_beginning_ept;
        this.pnode_name = pnode_name;
        this.system_energy_price_da = system_energy_price_da;
        this.total_lmp_da = total_lmp_da;
        this.congestion_price_da = congestion_price_da;
        this.marginal_loss_price_da = marginal_loss_price_da;
        this.total_da = total_da;
    }

    public String getDatetime_beginning_ept() {
        return datetime_beginning_ept;
    }

    public String getPnode_name() {
        return pnode_name;
    }

    public double getSystem_energy_price_da() {
        return system_energy_price_da;
    }

    public double getTotal_lmp_da() {
        return total_lmp_da;
    }

    public double getCongestion_price_da() {
        return congestion_price_da;
    }

    public double getMarginal_loss_price_da() {
        return marginal_loss_price_da;
    }

    public double getTotal_da() {
        return total_da;
    }

    public void setTotal_da(double total_da) {
        this.total_da = total_da;
    }
}
