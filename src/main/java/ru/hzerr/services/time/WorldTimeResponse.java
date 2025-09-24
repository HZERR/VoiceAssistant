package ru.hzerr.services.time;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorldTimeResponse {

    private String abbreviation;
    private String clientIp;
    private String datetime;
    private int dayOfWeek;
    private int dayOfYear;
    private boolean dst;
    private String dstFrom;
    private int dstOffset;
    private String dstUntil;
    private int rawOffset;
    private String timezone;
    private long unixtime;
    private String utcDatetime;
    private String utcOffset;
    private int weekNumber;

    public WorldTimeResponse() {
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @JsonProperty("client_ip")
    public String getClientIp() {
        return clientIp;
    }

    @JsonProperty("client_ip")
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @JsonProperty("day_of_week")
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    @JsonProperty("day_of_week")
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @JsonProperty("day_of_year")
    public int getDayOfYear() {
        return dayOfYear;
    }

    @JsonProperty("day_of_year")
    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public boolean isDst() {
        return dst;
    }

    public void setDst(boolean dst) {
        this.dst = dst;
    }

    @JsonProperty("dst_from")
    public String getDstFrom() {
        return dstFrom;
    }

    @JsonProperty("dst_from")
    public void setDstFrom(String dstFrom) {
        this.dstFrom = dstFrom;
    }

    @JsonProperty("dst_offset")
    public int getDstOffset() {
        return dstOffset;
    }

    @JsonProperty("dst_offset")
    public void setDstOffset(int dstOffset) {
        this.dstOffset = dstOffset;
    }

    @JsonProperty("dst_until")
    public String getDstUntil() {
        return dstUntil;
    }

    @JsonProperty("dst_until")
    public void setDstUntil(String dstUntil) {
        this.dstUntil = dstUntil;
    }

    @JsonProperty("raw_offset")
    public int getRawOffset() {
        return rawOffset;
    }

    @JsonProperty("raw_offset")
    public void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public long getUnixtime() {
        return unixtime;
    }

    public void setUnixtime(long unixtime) {
        this.unixtime = unixtime;
    }

    @JsonProperty("utc_datetime")
    public String getUtcDatetime() {
        return utcDatetime;
    }

    @JsonProperty("utc_datetime")
    public void setUtcDatetime(String utcDatetime) {
        this.utcDatetime = utcDatetime;
    }

    @JsonProperty("utc_offset")
    public String getUtcOffset() {
        return utcOffset;
    }

    @JsonProperty("utc_offset")
    public void setUtcOffset(String utcOffset) {
        this.utcOffset = utcOffset;
    }

    @JsonProperty("week_number")
    public int getWeekNumber() {
        return weekNumber;
    }

    @JsonProperty("week_number")
    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    @Override
    public String toString() {
        return "WorldTimeResponse{" +
                "abbreviation='" + abbreviation + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", datetime='" + datetime + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", dayOfYear=" + dayOfYear +
                ", dst=" + dst +
                ", dstFrom=" + dstFrom +
                ", dstOffset=" + dstOffset +
                ", dstUntil=" + dstUntil +
                ", rawOffset=" + rawOffset +
                ", timezone='" + timezone + '\'' +
                ", unixtime=" + unixtime +
                ", utcDatetime='" + utcDatetime + '\'' +
                ", utcOffset='" + utcOffset + '\'' +
                ", weekNumber=" + weekNumber +
                '}';
    }
}

