package com.team254.frc2015.auto;

import com.team254.frc2015.HardwareAdaptor;
import com.team254.frc2015.subsystems.Drive;
import com.team254.frc2015.subsystems.ElevatorCarriage;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public abstract class AutoMode extends AutoModeBase {

	protected Drive drive = HardwareAdaptor.kDrive;
    protected ElevatorCarriage top_carriage = HardwareAdaptor.kTopCarriage;
    protected ElevatorCarriage bottom_carriage = HardwareAdaptor.kBottomCarriage;
    protected PowerDistributionPanel pdp = HardwareAdaptor.kPDP;

}