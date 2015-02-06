package com.team254.lib.trajectory;

/**
 * PID + Feedforward controller for following a Trajectory.
 *
 * @author Jared341
 */
public class TrajectoryFollower {
	public static class TrajectoryConfig {
		public double dt;
		public double max_acc;
		public double max_vel;

		@Override
		public String toString() {
			return "dt: " + dt + ", max_acc: " + max_acc + ", max_vel: "
					+ max_vel;
		}
	}

	public static class TrajectorySetpoint {
		public double pos;
		public double vel;
		public double acc;

		@Override
		public String toString() {
			return "pos: " + pos + ", vel: " + vel + ", acc: " + acc;
		}
	}

	private double kp_;
	private double ki_;
	private double kd_;
	private double kv_;
	private double ka_;
	private double last_error_;
	private double error_sum_;
	private boolean reset_ = true;

	private TrajectoryConfig config_ = new TrajectoryConfig();
	private double goal_position_;
	private TrajectorySetpoint setpoint_ = new TrajectorySetpoint();

	public TrajectoryFollower() {
	}

	public void configure(double kp, double ki, double kd, double kv,
			double ka, TrajectoryConfig config) {
		kp_ = kp;
		ki_ = ki;
		kd_ = kd;
		kv_ = kv;
		ka_ = ka;
		config_ = config;
	}

	public void setGoal(TrajectorySetpoint current_state, double goal_position) {
		goal_position_ = goal_position;
		setpoint_ = current_state;
		reset_ = true;
		error_sum_ = 0.0;
	}

	public double calculate(double position, double velocity) {
		if (isFinishedTrajectory()) {
			setpoint_.pos = goal_position_;
			setpoint_.vel = 0;
			setpoint_.acc = 0;
			reset_ = true;
		} else {
			// Compute the new commanded position, velocity, and acceleration.
			double distance_to_go = goal_position_ - setpoint_.pos;
			double cur_vel = setpoint_.vel;
			double cur_vel2 = cur_vel * cur_vel;
			boolean inverted = false;
			if (distance_to_go < 0) {
				// System.out.println("inverted");
				inverted = true;
				distance_to_go *= -1;
				cur_vel *= -1;
			}
			// Compute discriminants of the minimum and maximum reachable
			// velocities over the remaining distance.
			double max_reachable_velocity_disc = cur_vel2 / 2.0
					+ config_.max_acc * distance_to_go;
			double min_reachable_velocity_disc = cur_vel2 / 2.0
					- config_.max_acc * distance_to_go;
			double cruise_vel = cur_vel;
			if (min_reachable_velocity_disc < 0 || cruise_vel < 0) {
				cruise_vel = Math.min(config_.max_vel,
						Math.sqrt(max_reachable_velocity_disc));
			}
			// System.out.println("Cruise vel " + cruise_vel + ", Cur vel " +
			// cur_vel);
			double t_start = (cruise_vel - cur_vel) / config_.max_acc; // Accelerate
																		// to
																		// cruise_vel
			double x_start = cur_vel * t_start + .5 * config_.max_acc * t_start
					* t_start;
			double t_end = Math.abs(cruise_vel / config_.max_acc); // Decelerate
																	// to zero
																	// vel.
			if (cruise_vel < 0) {
				// System.out.println("WHAT");
			}
			double x_end = cruise_vel * t_end - .5 * config_.max_acc * t_end
					* t_end;
			double x_cruise = Math.max(0, distance_to_go - x_start - x_end);
			double t_cruise = Math.abs(x_cruise / cruise_vel);
			// System.out.println(x_start + " " + x_cruise + " " + x_end + " " +
			// distance_to_go);
			// System.out.println(t_start + " " + t_cruise + " " + t_end);
			TrajectorySetpoint next_state = new TrajectorySetpoint();
			// Figure out where we should be one dt along this trajectory.
			// System.out.println("distance error " + (x_cruise + x_start +
			// x_end - distance_to_go));
			if (t_start >= config_.dt) {
				next_state.pos = cur_vel * config_.dt + .5 * config_.max_acc
						* config_.dt * config_.dt;
				next_state.vel = cur_vel + config_.max_acc * config_.dt;
				next_state.acc = config_.max_acc;
			} else if (t_start + t_cruise >= config_.dt) {
				next_state.pos = x_start + cruise_vel * (config_.dt - t_start);
				next_state.vel = cruise_vel;
				next_state.acc = 0;
			} else if (t_start + t_cruise + t_end >= config_.dt) {
				double delta_t = config_.dt - t_start - t_cruise;
				next_state.pos = x_start + x_cruise + cruise_vel * delta_t - .5
						* config_.max_acc * delta_t * delta_t;
				next_state.vel = cruise_vel - config_.max_acc * delta_t;
				next_state.acc = -config_.max_acc;
			} else {
				// Trajectory ends this cycle.
				next_state.pos = distance_to_go;
				next_state.vel = 0;
				next_state.acc = 0;
			}
			if (inverted) {
				next_state.pos *= -1;
				next_state.vel *= -1;
				next_state.acc *= -1;
			}
			setpoint_.pos += next_state.pos;
			setpoint_.vel = next_state.vel;
			setpoint_.acc = next_state.acc;

		}
		double error = setpoint_.pos - position;
		if (reset_) {
			// Prevent jump in derivative term when we have been reset.
			reset_ = false;
			last_error_ = error;
		}
		double output = kp_ * error + kd_
				* ((error - last_error_) / config_.dt - setpoint_.vel)
				+ (kv_ * setpoint_.vel + ka_ * setpoint_.acc);
		if (output < 1.0 && output > -1.0) {
			// Only integrate error if the output isn't already saturated.
			error_sum_ += error;
		}
		output += ki_ * error_sum_ * config_.dt;

		last_error_ = error;
		return output;
	}

	public boolean isFinishedTrajectory() {
		return Math.abs(setpoint_.pos - goal_position_) < 1E-3
				&& Math.abs(setpoint_.vel) < 1E-2;
	}

	public TrajectorySetpoint getCurrentSetpoint() {
		return setpoint_;
	}
}
