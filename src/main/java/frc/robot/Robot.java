// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import frc.robot.utils.I2CCOM;
import edu.wpi.first.wpilibj.*;
import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.*;


public class Robot extends TimedRobot {
  I2CCOM arduino;

  public int m_rainbowFirstPixelHue = 0;

  double motorSpeed = 0.55; //0.55 lowest speed 1 full speed
  double leftArmMove = 0;
  double rightArmMove = 0;

  DigitalInput leftArmSwitch = new DigitalInput(0);
  DigitalInput rightArmSwitch = new DigitalInput(1);

  private final PWMSparkMax m_leftTopMotor = new PWMSparkMax(0);
  private final PWMSparkMax m_leftBottomMotor = new PWMSparkMax(1);
  private final MotorControllerGroup m_left = new MotorControllerGroup(m_leftTopMotor, m_leftBottomMotor);

  private final PWMSparkMax m_rightTopMotor = new PWMSparkMax(2);
  private final PWMSparkMax m_rightBottomMotor = new PWMSparkMax(3);
  private final MotorControllerGroup m_right = new MotorControllerGroup(m_rightTopMotor, m_rightBottomMotor);

  private final PWMSparkMax m_leftArmMotor = new PWMSparkMax(8);
  private final PWMSparkMax m_rightArmMotor = new PWMSparkMax(7);

  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_left, m_right);
  private final Joystick m_stick = new Joystick(0);

  private boolean switched = false;

  AddressableLED m_led = new AddressableLED(9);
  AddressableLEDBuffer m_ledBuffer = new AddressableLEDBuffer(56);


  DoubleSolenoid head = new DoubleSolenoid(9, PneumaticsModuleType.CTREPCM, 0, 1);


  @Override
  public void robotInit() {
    m_right.setInverted(true);

    m_led.setLength(m_ledBuffer.getLength());
    m_led.start();

    head.set(kReverse);
    }

  @Override
  public void robotPeriodic() {
    rainbow();
    m_led.setData(m_ledBuffer);
  }

  @Override
  public void teleopInit() {
  //ran when teleop is enabled
  }

  @Override
  public void teleopPeriodic() {
    //System.out.println("Right Limit Switch held: " + rightArmSwitch.get() + ". Left Switch Held: " + leftArmSwitch.get());
    
    double turn = 0;

    if (m_stick.getRawButtonPressed(2)) {
      if(switched) {
        switched = false;
      } else {
        switched = true;
      }
    }

    if (switched == true) {
      turn = m_stick.getZ();  
    } else if(switched == false) {
      turn = m_stick.getX();
    }
    
    double speedPot = m_stick.getThrottle();
    motorSpeed = map(speedPot, 1, -1, 0.55, 1);

    Thread leftArm = new Thread() {
      public void run() {
        while(leftArmSwitch.get() == false) {
          m_leftArmMotor.set(1);
        }
        m_leftArmMotor.set(0);
        delay(200);
        m_leftArmMotor.set(-1);
        delay(550);
        m_leftArmMotor.set(0);
      }
    };

    Thread rightArm = new Thread() {
      public void run() {
        while(rightArmSwitch.get() == false) {
          m_rightArmMotor.set(1);
        }
        m_rightArmMotor.set(0);
        delay(200);
        m_rightArmMotor.set(-1);
        delay(550);
        m_rightArmMotor.set(0);
      }
    };

    Thread leftHalf = new Thread() {
      public void run() {
        if(leftArmSwitch.get() == false) {
          m_leftArmMotor.set(1);
          delay(350);
          m_leftArmMotor.set(0);
          delay(250);
          m_leftArmMotor.set(-1);
          delay(300);
          m_leftArmMotor.set(0);
        }
      }
    };

    Thread rightHalf = new Thread() {
      public void run() {
        if(rightArmSwitch.get() == false) {
          m_rightArmMotor.set(1);
          delay(350);
          m_rightArmMotor.set(0);
          delay(250);
          m_rightArmMotor.set(-1);
          delay(300);
          m_rightArmMotor.set(0);
        }
      }
    };

    if(m_stick.getRawButtonPressed(7)) {
      leftArm.start();
    }

    if(m_stick.getRawButtonPressed(8)){
      rightArm.start();
    }

    if(m_stick.getRawButtonPressed(9)) {
      leftHalf.start();
    }

    if(m_stick.getRawButtonPressed(10)) {
      rightHalf.start();
    }

    if(m_stick.getRawButtonPressed(11)) {
      head.toggle();
    }

    if(m_stick.getRawButtonPressed(12)) {
      //reserved
    }

    m_robotDrive.arcadeDrive(m_stick.getY() * motorSpeed, turn * motorSpeed);
  }

  public double map(double x, double in_min, double in_max, double out_min, double out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  }

  public static void delay(int millis){
    try{
      Thread.sleep(millis);
    }catch(Exception E){
    }
  }

  private void rainbow() {
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
      m_ledBuffer.setHSV(i, hue, 255, 128);
    }
    m_rainbowFirstPixelHue += 3;
    m_rainbowFirstPixelHue %= 180;
  }
}