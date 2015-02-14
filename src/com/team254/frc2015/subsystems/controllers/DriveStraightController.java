package com.team254.frc2015.subsystems.controllers;

import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.Pose.RelativePoseGenerator;
import com.team254.lib.util.SynchronousPID;
import com.team254.lib.trajectory.TrajectoryFollower.TrajectoryConfig;
import com.team254.lib.util.Pose;

public class DriveStraightController extends
		TrajectoryFollowingPositionController {

	private RelativePoseGenerator m_relative_pose_generator;
	private SynchronousPID pid;

	public DriveStraightController(double kp, double ki, double kd, double kv,
			double ka, double kpTurn, double kiTurn, double kdTurn,
			double on_target_delta, TrajectoryConfig config) {
		super(kp, ki, kd, kv, ka, on_target_delta, config);
		this.pid = new SynchronousPID();
		pid.setPID(kpTurn, kiTurn, kdTurn);
	}

	public void setStartPose(Pose pose) {
		m_relative_pose_generator = pose.new RelativePoseGenerator();
	}

	public DriveSignal update(Pose currentPose) {
		if (m_relative_pose_generator == null) {
            m_relative_pose_generator = currentPose.new RelativePoseGenerator();
		}

        double distance = (currentPose.getLeftDistance() + currentPose.getRightDistance()) / 2.0;
        double velocity = (currentPose.getLeftVelocity() + currentPose.getRightVelocity()) / 2.0;
        update(distance, velocity);
		double throttle = get();
		double turn = pid.calculate(currentPose.getHeading());

		return new DriveSignal(throttle + turn, throttle - turn);
	}

}