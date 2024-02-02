/*
Copyright 2023 FIRST Tech Challenge Team 10870

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.Gyroscope;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a PushBot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Remove a @Disabled the on the next line or two (if present) to add this opmode to the Driver Station OpMode list,
 * or add a @Disabled annotation to prevent this OpMode from being added to the Driver Station
 */
@Autonomous

public class CenterstageTickTest extends LinearOpMode {
    private Blinker control_Hub;
    private Blinker expansion_Hub;
    private DcMotorEx backLeft;
    private DcMotorEx backRight;
    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;
    //private DcMotor armPitch;
    //private Servo thumb;
    
    //Add
    static final double     SPEED_MULT              = 0.3;

    static final double     COUNTS_PER_MOTOR_REV    = 7.0; 
    static final double     DRIVE_GEAR_REDUCTION    = 60;   
    static final double     WHEEL_CIRCUMFERENCE_MM  = 75 * 3.14; //235.5
    
    static final double     COUNTS_PER_WHEEL_REV    = COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION; //420
    static final double     COUNTS_PER_MM           = COUNTS_PER_WHEEL_REV / WHEEL_CIRCUMFERENCE_MM; //~1.78

    static final double     DIST_PIXEL_TO_WALL      = 406.4; //mm
    static final double     DIST_MID_TO_WALL        = 1022.35; //mm
    static final double     DIST_DRIVE_FWRD         = DIST_MID_TO_WALL - DIST_PIXEL_TO_WALL; //mm

    static final double     SHIFT_LEFT      = 292.1; //mm*
    static final double     SHIFT_FWRD      = 146.05; //mm
    static final double     SHIFT_RIGHT      = 291.1; //mm*
    

    int Zone = 1; //0 = Left, 1 = Forward, 2 = Right
    
    @Override
    public void runOpMode() {
        control_Hub = hardwareMap.get(Blinker.class, "Control Hub");
        expansion_Hub = hardwareMap.get(Blinker.class, "Expansion Hub");
        
        //May want to reverse dirrection here if the robot will be going backwards
        backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "backRight");
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        
        //armPitch = hardwareMap.get(DcMotor.class, "armPitch");
        //thumb = hardwareMap.get(Servo.class, "thumb");

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        
        //Add:
        double BL_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;         
        double FL_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;
        double BR_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;         
        double FR_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;

        int BL_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);
        int FL_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);
        int BR_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM); 
        int FR_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);

        waitForStart();
        
        backLeft.setVelocity(BL_TPS);
        frontLeft.setVelocity(FL_TPS);
        backRight.setVelocity(BR_TPS);
        frontRight.setVelocity(FR_TPS);

        backLeft.setTargetPosition(BL_Target);
        frontLeft.setTargetPosition(FL_Target);
        backRight.setTargetPosition(BR_Target);
        frontRight.setTargetPosition(FR_Target);
        
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // backLeft.setPower(0.5);
        // frontLeft.setPower(0.5);
        // backRight.setPower(0.5);
        // frontRight.setPower(0.5);
        
        // opModeIsActive runs until the end of the match (driver presses STOP)
        while (opModeIsActive() && (backLeft.isBusy() && frontLeft.isBusy() && backRight.isBusy() && frontRight.isBusy())) {
            telemetry.addData("Status", "Running");
            telemetry.addData("backLeft", backLeft.getCurrentPosition());
            telemetry.addData("frontLeft", frontLeft.getCurrentPosition());
            telemetry.addData("backRight", backRight.getCurrentPosition());
            telemetry.addData("frontRight", frontRight.getCurrentPosition());
            telemetry.update();
        }

        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        //Add all below:
        if (zone == 0){
            //Left
            frontLeft.setDirection(DcMotor.Direction.FORWARD);
            backRight.setDirection(DcMotor.Direction.REVERSE);

            BL_Target = (int)(SHIFT_LEFT * COUNTS_PER_MM);
            FL_Target = (int)(SHIFT_LEFT * COUNTS_PER_MM);
            BR_Target = (int)(SHIFT_LEFT * COUNTS_PER_MM); 
            FR_Target = (int)(SHIFT_LEFT * COUNTS_PER_MM);
        }
        else if (zone == 1){
            //forward

            BL_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
            FL_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
            BR_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM); 
            FR_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
        }
        else {
            //right
            backLeft.setDirection(DcMotor.Direction.FORWARD);
            frontRight.setDirection(DcMotor.Direction.REVERSE);

            BL_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
            FL_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
            BR_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM); 
            FR_Target = (int)(SHIFT_FWRD * COUNTS_PER_MM);
        }

        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // opModeIsActive runs until the end of the match (driver presses STOP)
        while (opModeIsActive() && (backLeft.isBusy() && frontLeft.isBusy() && backRight.isBusy() && frontRight.isBusy())) {
            telemetry.addData("Status", "Running");
            telemetry.addData("backLeft", backLeft.getCurrentPosition());
            telemetry.addData("frontLeft", frontLeft.getCurrentPosition());
            telemetry.addData("backRight", backRight.getCurrentPosition());
            telemetry.addData("frontRight", frontRight.getCurrentPosition());
            telemetry.update();
        }

        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

    }
}
