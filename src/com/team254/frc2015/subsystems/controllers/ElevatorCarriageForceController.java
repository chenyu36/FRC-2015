package com.team254.frc2015.subsystems.controllers;

import com.team254.frc2015.HardwareAdaptor;
import com.team254.frc2015.subsystems.BottomCarriage;
import com.team254.frc2015.subsystems.ElevatorCarriage;
import com.team254.lib.util.CheesySpeedController;
import com.team254.lib.util.Controller;

public class ElevatorCarriageForceController extends Controller {

    CheesySpeedController m_top_carriage = HardwareAdaptor.kTopCarriageMotor;
    CheesySpeedController m_bottom_carriage = HardwareAdaptor.kBottomCarriageMotor;

    double m_squeeze_power = 0;
    boolean m_follow_bottom = true;

    public ElevatorCarriageForceController(ElevatorCarriage follower) {
        if (follower instanceof BottomCarriage) {
            m_follow_bottom = false;
        } else {
            m_follow_bottom = true;
        }
    }

    public void setSqueezePower(double squeeze_power) {
        m_squeeze_power = squeeze_power;
    }

    @Override
    public void reset() {}

    public double update() {
        if (m_follow_bottom) {
            return m_bottom_carriage.get() - m_squeeze_power;
        } else {
            return m_top_carriage.get() + m_squeeze_power;
        }
    }

    @Override
    public boolean isOnTarget() {
        // This controller is a best effort controller.
        return false;
    }

}
