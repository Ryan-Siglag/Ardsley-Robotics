/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.List;

/*
 * This OpMode illustrates the basics of TensorFlow Object Detection,
 * including Java Builder structures for specifying Vision parameters.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
@Autonomous(name = "Concept: TensorFlow Object Detection", group = "Concept")

public class ATeleopTest extends LinearOpMode {
    
    private Blinker control_Hub;
    private Blinker expansion_Hub;
    private DcMotorEx backLeft;
    private DcMotorEx backRight;
    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;
    private DcMotor armPitch;
    private Servo thumb;

    private ElapsedTime
    runtime = new ElapsedTime();
    
    private static final String TFOD_MODEL_FILE = "cts.tflite"; 
    // private static final String TFOD_MODEL_FILE  = "/sdcard/FIRST/tflitemodels/CustomTeamModel.tflite";


    private static final String[] LABELS = {
            "blue",
            "red"
    };

    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    /**
     * The variable to store our instance of the TensorFlow Object Detection processor.
     */
    private TfodProcessor tfod;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;
    
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
    
    int zone = 1;

    @Override
    public void runOpMode() {
        
        control_Hub = hardwareMap.get(Blinker.class, "Control Hub");
        expansion_Hub = hardwareMap.get(Blinker.class, "Expansion Hub");
        
        //May want to reverse dirrection here if the robot will be going backwards
        backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "backRight");
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        
        initTfod();

        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();
        waitForStart();
        
       
        
        if (opModeIsActive() && runtime.seconds() <= 2) {
            while (opModeIsActive()) {

                telemetryTfod();

                // Push telemetry to the Driver Station.
                telemetry.update();
                    
                // Save CPU resources; can resume streaming when needed.
                // if (gamepad1.dpad_down) {
                //     visionPortal.stopStreaming();
                // } else if (gamepad1.dpad_up) {
                //     visionPortal.resumeStreaming();
                // }

                // Share the CPU.
                //sleep(20);
            }
        }

        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        
        
        double BL_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;         
        double FL_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;
        double BR_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;         
        double FR_TPS = (175.0/ 60) * COUNTS_PER_WHEEL_REV * SPEED_MULT;

        int BL_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);
        int FL_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);
        int BR_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM); 
        int FR_Target = (int)(DIST_DRIVE_FWRD * COUNTS_PER_MM);

        //waitForStart();
        
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
            telemetry.addData("targetZone", zone);
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
        
                    
                

        // Save more CPU resources when camera is no longer needed.
        //visionPortal.close();

    }   // end runOpMode()

    /**
     * Initialize the TensorFlow Object Detection processor.
     */
    private void initTfod() {

        // Create the TensorFlow processor by using a builder.
        tfod = new TfodProcessor.Builder()
        

            // Use setModelAssetName() if the TF Model is built in as an asset.
            // Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
            //.setModelAssetName(TFOD_MODEL_ASSET)
            .setModelFileName(TFOD_MODEL_FILE)

            .setModelLabels(LABELS)
            //.setIsModelTensorFlow2(true)
            //.setIsModelQuantized(true)
            //.setModelInputSize(300)
            //.setModelAspectRatio(16.0 / 9.0)

            .build();
        //tfod.loadModelFromFile(TFOD_MODEL_ASSET, LABELS);
        // Create the vision portal by using a builder.
        VisionPortal.Builder builder = new VisionPortal.Builder();

        // Set the camera (webcam vs. built-in RC phone camera).
        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }

        // Choose a camera resolution. Not all cameras support all resolutions.
        //builder.setCameraResolution(new Size(640, 480));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        //builder.enableCameraMonitoring(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(tfod);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Set confidence threshold for TFOD recognitions, at any time.
        //tfod.setMinResultConfidence(0.75f);

        // Disable or re-enable the TFOD processor at any time.
        //visionPortal.setProcessorEnabled(tfod, true);

    }   // end method initTfod()

    /**
     * Add telemetry about TensorFlow Object Detection (TFOD) recognitions.
     */
    private void telemetryTfod() {

        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());

        // Step through the list of recognitions and display info for each one.
        for (Recognition recognition : currentRecognitions) {
            double x = (recognition.getLeft() + recognition.getRight()) / 2 ;
            double y = (recognition.getTop()  + recognition.getBottom()) / 2 ;
            
            if (x < 200) {
                zone = 0;
            } 
            else if (x < 400) {
                zone = 1;
            }
            else {
                zone = 2;
            }
            telemetry.addData("Zone", zone);
        
            
            telemetry.addData(""," ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            telemetry.addData("- Position", "%.0f / %.0f", x, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
        }   // end for() loop

    }   // end method telemetryTfod()

}   // end class
