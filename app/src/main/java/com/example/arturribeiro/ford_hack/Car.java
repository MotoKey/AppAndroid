package com.example.arturribeiro.ford_hack;

import com.smartdevicelink.proxy.rpc.AirbagStatus;
import com.smartdevicelink.proxy.rpc.enums.IgnitionStatus;

/**
 * Created by arturribeiro on 20/05/17.
 */
public class Car {

    Boolean userConected;
    Double velocity;
    Boolean ignition;
    Boolean  airbagStatus;
    Boolean doors;
    Double fuelLevel;

    public Integer getOdometer() {
        return odometer;
    }

    public void setOdometer(Integer odometer) {
        this.odometer = odometer;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Boolean getAirbagStatus() {
        return airbagStatus;
    }

    public void setAirbagStatus(Boolean airbagStatus) {
        this.airbagStatus = airbagStatus;
    }

    public Boolean getDoors() {
        return doors;
    }

    public void setDoors(Boolean doors) {
        this.doors = doors;
    }

    public Boolean getIgnition() {
        return ignition;
    }

    public void setIgnition(Boolean ignition) {
        this.ignition = ignition;
    }

    Integer odometer;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    String json;

    public Boolean getUserConected() {
        return userConected;
    }

    public void setUserConected(Boolean userConected) {
        this.userConected = userConected;
    }

    public Double getVelocity() {
        return velocity;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }
}
