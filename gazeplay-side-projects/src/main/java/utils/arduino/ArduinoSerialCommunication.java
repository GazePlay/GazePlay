package utils.arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
public class ArduinoSerialCommunication implements SerialPortEventListener {
    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String[] PORT_NAMES = {"/dev/tty.HC-06-DevB", // bluetooth
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyACM0", // Raspberry Pi
        "/dev/ttyUSB0", // Linux
        "COM3", // Windows
        "/dev/cu.usbmodem1421", // Mon arduino
        "/dev/cu.usbmodem1411", // Mon arduino
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader converting the bytes into characters making the
     * displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;

    public void initialize() {
        // the next line is for Raspberry Pi and
        // gets us into the while loop and was suggested here was suggested
        // http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        // System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
        // System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/tty.usbserial-A9007UX1");

        CommPortIdentifier portId = null;
        final Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            final CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            log.info(currPortId.getName());
            for (final String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    log.info("Found : " + portName);
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            log.info("Could not find COM port.");
            System.exit(0);
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), StandardCharsets.UTF_8));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (final Exception e) {
            log.error("error 1 " + e.toString(), e);
            System.exit(0);
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public void sendArduino(final String s) {

        try {
            log.info("Envoi : " + s);
            output.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            log.error("Exception", e);
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    @Override
    public synchronized void serialEvent(final SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                final String inputLine = input.readLine();
                log.info("Reçoit : " + inputLine);
            } catch (final Exception e) {
                log.error("error 2 " + e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static void main(final String[] args) throws Exception {
        final ArduinoSerialCommunication arduino = new ArduinoSerialCommunication();
        arduino.initialize();

        log.info("Started");

        while (true) {
            arduino.sendArduino("L");
            Thread.sleep(2000);
            arduino.sendArduino("M");
            Thread.sleep(10000);
        }

    }
}
