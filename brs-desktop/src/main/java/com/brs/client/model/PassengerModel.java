package com.brs.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassengerModel {
    private String seatNumber;
    private String fullName;
    private Integer age;
    private String gender; // MALE, FEMALE, OTHER

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String seatNumber;
        private String fullName;
        private Integer age;
        private String gender;

        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public PassengerModel build() { return new PassengerModel(seatNumber, fullName, age, gender); }
    }
}
