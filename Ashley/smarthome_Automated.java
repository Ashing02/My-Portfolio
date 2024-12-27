import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

abstract class SmartDevice {
    private final String name;
    private int energyConsumption; // Energy consumption in watts

    public SmartDevice(String name, int initialConsumption) {
        this.name = name;
        this.energyConsumption = initialConsumption;
    }

    public String getName() {
        return name;
    }

    public int getEnergyConsumption() {
        return energyConsumption;
    }

    public void addEnergyConsumption(int energy) {
        this.energyConsumption += energy;
    }

    public abstract JPanel getControlPanel();

    public abstract void setVacationMode(boolean isActive);
}

class SmartOven extends SmartDevice {
    private boolean isOn;
    private int temperature;
    private Timer cookingTimer;
    private JLabel timerLabel;
    private TimerTask cookingTask;

    public SmartOven(String name) {
        super(name, 10);
        isOn = false;
        temperature = 350;
        cookingTimer = new Timer();
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(getName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 128, 255));
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton onButton = new JButton("Turn On");
        onButton.setBackground(new Color(0, 255, 0));
        panel.add(onButton, gbc);

        gbc.gridx++;
        JButton offButton = new JButton("Turn Off");
        offButton.setBackground(new Color(245, 245, 245));
        panel.add(offButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JSlider temperatureSlider = new JSlider(100, 500, temperature);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setMajorTickSpacing(100);
        temperatureSlider.setMinorTickSpacing(25);
        temperatureSlider.setPaintLabels(true);
        panel.add(temperatureSlider, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel minutesLabel = new JLabel("Minutes:");
        panel.add(minutesLabel, gbc);

        gbc.gridx++;
        JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        panel.add(minutesSpinner, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton startTimerButton = new JButton("Start Timer");
        startTimerButton.setBackground(new Color(0, 120, 215));
        startTimerButton.setForeground(Color.WHITE);
        panel.add(startTimerButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        timerLabel = new JLabel("Timer: 00:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(timerLabel, gbc);

        onButton.addActionListener(e -> {
            isOn = true;
            addEnergyConsumption(5); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " turned on");
            JOptionPane.showMessageDialog(null, getName() + " turned on.", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        offButton.addActionListener(e -> {
            isOn = false;
            addEnergyConsumption(-5); // Simulate energy consumption
            if (cookingTask != null) {
                cookingTask.cancel();
            }
            SmartHomeAutomationSystem.logHistory(getName() + " turned off");
            JOptionPane.showMessageDialog(null, getName() + " turned off.", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        temperatureSlider.addChangeListener(e -> {
            temperature = temperatureSlider.getValue();
            SmartHomeAutomationSystem.logHistory(getName() + " temperature set to " + temperature + "°F");
            JOptionPane.showMessageDialog(null, getName() + " temperature set to " + temperature + "°F.", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        startTimerButton.addActionListener(e -> {
            if (!isOn) {
                JOptionPane.showMessageDialog(null, "Please turn on the oven first.", "Action Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int minutes = (int) minutesSpinner.getValue();
            if (minutes <= 0) {
                JOptionPane.showMessageDialog(null, "Please set a valid timer.", "Action Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            startCookingTimer(minutes);
            SmartHomeAutomationSystem.logHistory(getName() + " timer set for " + minutes + " minutes");
            JOptionPane.showMessageDialog(null, getName() + " timer set for " + minutes + " minutes.", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    private void startCookingTimer(int minutes) {
        if (cookingTask != null) {
            cookingTask.cancel();
        }

        cookingTask = new TimerTask() {
            int timeRemaining = minutes * 60;

            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    timerLabel.setText("Timer: 00:00");
                    SmartHomeAutomationSystem.logHistory(getName() + " timer finished");
                    JOptionPane.showMessageDialog(null, getName() + " timer finished.", "Action Info", JOptionPane.INFORMATION_MESSAGE);
                    this.cancel();
                    return;
                }
                timeRemaining--;
                int mins = timeRemaining / 60;
                int secs = timeRemaining % 60;
                timerLabel.setText(String.format("Timer: %02d:%02d", mins, secs));
            }
        };

        cookingTimer.scheduleAtFixedRate(cookingTask, 0, 1000);
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            isOn = false; // Turn off the oven
            if (cookingTask != null) {
                cookingTask.cancel();
            }
            SmartHomeAutomationSystem.logHistory(getName() + " turned off for vacation mode.");
        } else {
            isOn = false; // Ensure it's off
            if (cookingTask != null) {
                cookingTask.cancel();
            }
            SmartHomeAutomationSystem.logHistory(getName() + " reset after vacation.");
        }
    }
}

class Light extends SmartDevice {
    private boolean isOn;
    private int brightness;
    private String selectedLight;
    private Timer vacationTimer;

    public Light(String name) {
        super(name, 10);
        isOn = false;
        brightness = 50;
        selectedLight = "Living Room Light";
        vacationTimer = new Timer();
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("LIGHT CONTROL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(new Color(0, 120, 215));
        titleLabel.setOpaque(true);
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel selectLightLabel = new JLabel("Select Light:");
        panel.add(selectLightLabel, gbc);

        gbc.gridx++;
        JComboBox<String> lightComboBox = new JComboBox<>(new String[]{"Living Room Light", "Bedroom Light", "Kitchen Light", "Bathroom Light"});
        lightComboBox.setSelectedItem(selectedLight);
        panel.add(lightComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel ceilingLabel = new JLabel("Ceiling:");
        panel.add(ceilingLabel, gbc);

        gbc.gridx++;
        JSlider ceilingSlider = new JSlider(0, 100, brightness);
        ceilingSlider.setPaintTicks(true);
        ceilingSlider.setMajorTickSpacing(20);
        ceilingSlider.setMinorTickSpacing(5);
        ceilingSlider.setPaintLabels(true);
        ceilingSlider.setBackground(new Color(245, 245, 245));
        panel.add(ceilingSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lampsLabel = new JLabel("Lamps:");
        panel.add(lampsLabel, gbc);

        gbc.gridx++;
        JSlider lampsSlider = new JSlider(0, 100, brightness);
        lampsSlider.setPaintTicks(true);
        lampsSlider.setMajorTickSpacing(20);
        lampsSlider.setMinorTickSpacing(5);
        lampsSlider.setPaintLabels(true);
        lampsSlider.setBackground(new Color(245, 245, 245));
        panel.add(lampsSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel readingLabel = new JLabel("Reading:");
        panel.add(readingLabel, gbc);

        gbc.gridx++;
        JSlider readingSlider = new JSlider(0, 100, brightness);
        readingSlider.setPaintTicks(true);
        readingSlider.setMajorTickSpacing(20);
        readingSlider.setMinorTickSpacing(5);
        readingSlider.setPaintLabels(true);
        readingSlider.setBackground(new Color(245, 245, 245));
        panel.add(readingSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel colorLabel = new JLabel("Color:");
        panel.add(colorLabel, gbc);

        gbc.gridx++;
        JComboBox<String> colorComboBox = new JComboBox<>(new String[]{"White", "Warm White", "Cool White", "Red", "Green", "Blue"});
        panel.add(colorComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton partyButton = new JButton("Party");
        partyButton.setBackground(new Color(0, 120, 215));
        partyButton.setForeground(Color.WHITE);
        panel.add(partyButton, gbc);

        lightComboBox.addActionListener(e -> {
            selectedLight = (String) lightComboBox.getSelectedItem();
            SmartHomeAutomationSystem.logHistory(getName() + " changed to " + selectedLight);
            JOptionPane.showMessageDialog(null, getName() + " changed to " + selectedLight, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        ceilingSlider.addChangeListener(e -> {
            brightness = ceilingSlider.getValue();
            addEnergyConsumption(brightness / 5); // Simulate energy consumption increase with brightness
            SmartHomeAutomationSystem.logHistory(selectedLight + " ceiling brightness set to " + brightness);
            JOptionPane.showMessageDialog(null, selectedLight + " ceiling brightness set to " + brightness, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        lampsSlider.addChangeListener(e -> {
            brightness = lampsSlider.getValue();
            addEnergyConsumption(brightness / 5); // Simulate energy consumption increase with brightness
            SmartHomeAutomationSystem.logHistory(selectedLight + " lamps brightness set to " + brightness);
            JOptionPane.showMessageDialog(null, selectedLight + " lamps brightness set to " + brightness, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        readingSlider.addChangeListener(e -> {
            brightness = readingSlider.getValue();
            addEnergyConsumption(brightness / 5); // Simulate energy consumption increase with brightness
            SmartHomeAutomationSystem.logHistory(selectedLight + " reading brightness set to " + brightness);
            JOptionPane.showMessageDialog(null, selectedLight + " reading brightness set to " + brightness, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        colorComboBox.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(selectedLight + " color set to " + colorComboBox.getSelectedItem());
            JOptionPane.showMessageDialog(null, selectedLight + " color set to " + colorComboBox.getSelectedItem(), "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        partyButton.addActionListener(e -> {
            ceilingSlider.setValue(100);
            lampsSlider.setValue(100);
            readingSlider.setValue(100);
            colorComboBox.setSelectedItem("Red");
            SmartHomeAutomationSystem.logHistory(selectedLight + " party mode activated");
            JOptionPane.showMessageDialog(null, selectedLight + " party mode activated", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            vacationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int randomBrightness = new Random().nextInt(101);
                    brightness = randomBrightness;
                    SmartHomeAutomationSystem.logHistory(getName() + " brightness randomly set to " + brightness + " for vacation mode.");
                }
            }, 0, 60000); // Change brightness every minute
        } else {
            vacationTimer.cancel();
            brightness = 0; // Turn off the lights
            SmartHomeAutomationSystem.logHistory(getName() + " turned off for vacation mode.");
        }
    }
}

class Thermostat extends SmartDevice {
    private int temperature;
    private String selectedThermostat;

    public Thermostat(String name) {
        super(name, 5);
        temperature = 70;
        selectedThermostat = "Living Room Thermostat";
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("TEMPERATURE CONTROL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(new Color(0, 120, 215));
        titleLabel.setOpaque(true);
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel selectThermostatLabel = new JLabel("Select Thermostat:");
        panel.add(selectThermostatLabel, gbc);

        gbc.gridx++;
        JComboBox<String> thermostatComboBox = new JComboBox<>(new String[]{"Living Room Thermostat", "Bedroom Thermostat", "Kitchen Thermostat"});
        thermostatComboBox.setSelectedItem(selectedThermostat);
        panel.add(thermostatComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JButton decreaseTempButton = new JButton("-");
        decreaseTempButton.setBackground(new Color(0, 120, 215));
        decreaseTempButton.setForeground(Color.WHITE);
        panel.add(decreaseTempButton, gbc);

        gbc.gridx++;
        JSlider temperatureSlider = new JSlider(60, 80, temperature);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setMajorTickSpacing(5);
        temperatureSlider.setMinorTickSpacing(1);
        temperatureSlider.setPaintLabels(true);
        temperatureSlider.setBackground(new Color(245, 245, 245));
        panel.add(temperatureSlider, gbc);

        gbc.gridx++;
        JButton increaseTempButton = new JButton("+");
        increaseTempButton.setBackground(new Color(0, 120, 215));
        increaseTempButton.setForeground(Color.WHITE);
        panel.add(increaseTempButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton energySavingModeButton = new JButton("Energy Saving Mode");
        energySavingModeButton.setBackground(new Color(0, 120, 215));
        energySavingModeButton.setForeground(Color.WHITE);
        panel.add(energySavingModeButton, gbc);

        thermostatComboBox.addActionListener(e -> {
            selectedThermostat = (String) thermostatComboBox.getSelectedItem();
            SmartHomeAutomationSystem.logHistory(getName() + " changed to " + selectedThermostat);
            JOptionPane.showMessageDialog(null, getName() + " changed to " + selectedThermostat, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        increaseTempButton.addActionListener(e -> {
            temperatureSlider.setValue(temperatureSlider.getValue() + 1);
        });
        decreaseTempButton.addActionListener(e -> {
            temperatureSlider.setValue(temperatureSlider.getValue() - 1);
        });
        temperatureSlider.addChangeListener(e -> {
            temperature = temperatureSlider.getValue();
            addEnergyConsumption(1); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(selectedThermostat + " temperature set to " + temperature);
            JOptionPane.showMessageDialog(null, selectedThermostat + " temperature set to " + temperature, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        energySavingModeButton.addActionListener(e -> {
            temperatureSlider.setValue(65);
            SmartHomeAutomationSystem.logHistory(selectedThermostat + " energy saving mode activated");
            JOptionPane.showMessageDialog(null, selectedThermostat + " energy saving mode activated", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            temperature = 60; // Set to a lower temperature for energy saving
            SmartHomeAutomationSystem.logHistory(getName() + " set to energy saving mode for vacation.");
        } else {
            temperature = 70; // Reset to normal temperature
            SmartHomeAutomationSystem.logHistory(getName() + " reset to normal mode.");
        }
    }
}

class SecurityCamera extends SmartDevice {
    private boolean isActivated;

    public SecurityCamera(String name) {
        super(name, 15);
        isActivated = false;
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("SECURITY CONTROL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(new Color(0, 120, 215));
        titleLabel.setOpaque(true);
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel statusLabel = new JLabel("System Armed", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(statusLabel, gbc);

        gbc.gridx++;
        JButton armButton = new JButton("ARM");
        armButton.setBackground(new Color(0, 120, 215));
        armButton.setForeground(Color.WHITE);
        panel.add(armButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel frontDoorLabel = new JLabel("Front Door");
        panel.add(frontDoorLabel, gbc);

        gbc.gridx++;
        JToggleButton frontDoorToggle = new JToggleButton("Unlocked");
        panel.add(frontDoorToggle, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel backDoorLabel = new JLabel("Back Door");
        panel.add(backDoorLabel, gbc);

        gbc.gridx++;
        JToggleButton backDoorToggle = new JToggleButton("Unlocked");
        panel.add(backDoorToggle, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel garageDoorLabel = new JLabel("Garage Door");
        panel.add(garageDoorLabel, gbc);

        gbc.gridx++;
        JToggleButton garageDoorToggle = new JToggleButton("Unlocked");
        panel.add(garageDoorToggle, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton activateAllButton = new JButton("Activate All");
        activateAllButton.setBackground(new Color(0, 120, 215));
        activateAllButton.setForeground(Color.WHITE);
        panel.add(activateAllButton, gbc);

        armButton.addActionListener(e -> {
            isActivated = !isActivated;
            statusLabel.setText(isActivated ? "System Armed" : "System Disarmed");
            armButton.setText(isActivated ? "DISARM" : "ARM");
            addEnergyConsumption(isActivated ? 5 : -5); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " armed status changed to " + isActivated);
            JOptionPane.showMessageDialog(null, getName() + " armed status changed to " + isActivated, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        frontDoorToggle.addActionListener(e -> {
            String status = frontDoorToggle.isSelected() ? "Locked" : "Unlocked";
            frontDoorToggle.setText(status);
            SmartHomeAutomationSystem.logHistory("Front door " + status);
            JOptionPane.showMessageDialog(null, "Front door " + status, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        backDoorToggle.addActionListener(e -> {
            String status = backDoorToggle.isSelected() ? "Locked" : "Unlocked";
            backDoorToggle.setText(status);
            SmartHomeAutomationSystem.logHistory("Back door " + status);
            JOptionPane.showMessageDialog(null, "Back door " + status, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        garageDoorToggle.addActionListener(e -> {
            String status = garageDoorToggle.isSelected() ? "Locked" : "Unlocked";
            garageDoorToggle.setText(status);
            SmartHomeAutomationSystem.logHistory("Garage door " + status);
            JOptionPane.showMessageDialog(null, "Garage door " + status, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        activateAllButton.addActionListener(e -> {
            frontDoorToggle.setSelected(true);
            backDoorToggle.setSelected(true);
            garageDoorToggle.setSelected(true);
            frontDoorToggle.setText("Locked");
            backDoorToggle.setText("Locked");
            garageDoorToggle.setText("Locked");
            SmartHomeAutomationSystem.logHistory("All doors locked");
            JOptionPane.showMessageDialog(null, "All doors locked", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            isActivated = true; // Arm the system
            SmartHomeAutomationSystem.logHistory(getName() + " armed for vacation.");
        } else {
            isActivated = false; // Disarm the system
            SmartHomeAutomationSystem.logHistory(getName() + " disarmed after vacation.");
        }
    }
}

class SmartSpeaker extends SmartDevice {
    private boolean isPlaying;
    private String selectedPlaylist;
    private Timer vacationTimer;

    public SmartSpeaker(String name) {
        super(name, 5);
        isPlaying = false;
        selectedPlaylist = "Top Hits";
        vacationTimer = new Timer();
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("MUSIC CONTROL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(new Color(0, 120, 215));
        titleLabel.setOpaque(true);
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel selectPlaylistLabel = new JLabel("Select Playlist:");
        panel.add(selectPlaylistLabel, gbc);

        gbc.gridx++;
        JComboBox<String> playlistComboBox = new JComboBox<>(new String[]{"Top Hits", "Classical Music", "Jazz", "Rock", "Pop"});
        playlistComboBox.setSelectedItem(selectedPlaylist);
        panel.add(playlistComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel songLabel = new JLabel("Forever young (dance mix)", JLabel.CENTER);
        panel.add(songLabel, gbc);

        gbc.gridx++;
        JButton playButton = new JButton("Play");
        playButton.setBackground(new Color(0, 120, 215));
        playButton.setForeground(Color.WHITE);
        panel.add(playButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JButton stopButton = new JButton("Stop");
        stopButton.setBackground(new Color(0, 120, 215));
        stopButton.setForeground(Color.WHITE);
        panel.add(stopButton, gbc);

        gbc.gridx++;
        JButton previousButton = new JButton("Previous");
        previousButton.setBackground(new Color(0, 120, 215));
        previousButton.setForeground(Color.WHITE);
        panel.add(previousButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JButton nextButton = new JButton("Next");
        nextButton.setBackground(new Color(0, 120, 215));
        nextButton.setForeground(Color.WHITE);
        panel.add(nextButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setBackground(new Color(245, 245, 245));
        panel.add(volumeSlider, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JButton stopAllButton = new JButton("Stop All");
        stopAllButton.setBackground(new Color(0, 120, 215));
        stopAllButton.setForeground(Color.WHITE);
        panel.add(stopAllButton, gbc);

        playlistComboBox.addActionListener(e -> {
            selectedPlaylist = (String) playlistComboBox.getSelectedItem();
            SmartHomeAutomationSystem.logHistory(getName() + " changed to playlist " + selectedPlaylist);
            JOptionPane.showMessageDialog(null, getName() + " changed to playlist " + selectedPlaylist, "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        playButton.addActionListener(e -> {
            isPlaying = true;
            addEnergyConsumption(1); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " started playing music");
            JOptionPane.showMessageDialog(null, getName() + " started playing music", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        stopButton.addActionListener(e -> {
            isPlaying = false;
            addEnergyConsumption(-1); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " stopped playing music");
            JOptionPane.showMessageDialog(null, getName() + " stopped playing music", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        previousButton.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " previous song");
            JOptionPane.showMessageDialog(null, getName() + " previous song", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        nextButton.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " next song");
            JOptionPane.showMessageDialog(null, getName() + " next song", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        volumeSlider.addChangeListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " volume set to " + volumeSlider.getValue());
            JOptionPane.showMessageDialog(null, getName() + " volume set to " + volumeSlider.getValue(), "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        stopAllButton.addActionListener(e -> {
            isPlaying = false;
            SmartHomeAutomationSystem.logHistory(getName() + " stopped all music");
            JOptionPane.showMessageDialog(null, getName() + " stopped all music", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            vacationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isPlaying = !isPlaying;
                    SmartHomeAutomationSystem.logHistory(getName() + " playing state toggled for vacation mode.");
                }
            }, 0, 180000); // Toggle playing state every 3 minutes
        } else {
            vacationTimer.cancel();
            isPlaying = false; // Stop playing music
            SmartHomeAutomationSystem.logHistory(getName() + " stopped playing for vacation mode.");
        }
    }
}

class SmartWindowBlinds extends SmartDevice {
    private int position;

    public SmartWindowBlinds(String name) {
        super(name, 3);
        position = 50;
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(getName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 128, 255));
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton openButton = new JButton("Open Blinds");
        openButton.setBackground(new Color(0, 255, 0));
        panel.add(openButton, gbc);

        gbc.gridx++;
        JButton closeButton = new JButton("Close Blinds");
        closeButton.setBackground(new Color(245, 245, 245));
        panel.add(closeButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JSlider positionSlider = new JSlider(0, 100, position);
        positionSlider.setPaintTicks(true);
        positionSlider.setMajorTickSpacing(20);
        positionSlider.setMinorTickSpacing(5);
        positionSlider.setPaintLabels(true);
        panel.add(positionSlider, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JButton positionHalfButton = new JButton("Set to 50%");
        positionHalfButton.setBackground(new Color(0, 120, 215));
        positionHalfButton.setForeground(Color.WHITE);
        panel.add(positionHalfButton, gbc);

        openButton.addActionListener(e -> {
            positionSlider.setValue(100);
            SmartHomeAutomationSystem.logHistory(getName() + " opened blinds");
            JOptionPane.showMessageDialog(null, getName() + " opened blinds", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        closeButton.addActionListener(e -> {
            positionSlider.setValue(0);
            SmartHomeAutomationSystem.logHistory(getName() + " closed blinds");
            JOptionPane.showMessageDialog(null, getName() + " closed blinds", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        positionSlider.addChangeListener(e -> {
            position = positionSlider.getValue();
            addEnergyConsumption(1); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " blinds set to " + position + "% open");
            JOptionPane.showMessageDialog(null, getName() + " blinds set to " + position + "% open", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        positionHalfButton.addActionListener(e -> {
            positionSlider.setValue(50);
            SmartHomeAutomationSystem.logHistory(getName() + " blinds set to 50%");
            JOptionPane.showMessageDialog(null, getName() + " blinds set to 50%", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            position = 50; // Set to halfway open
            SmartHomeAutomationSystem.logHistory(getName() + " set to halfway open for vacation.");
        } else {
            position = 0; // Close the blinds
            SmartHomeAutomationSystem.logHistory(getName() + " closed after vacation.");
        }
    }
}

class SmartCoffeeMaker extends SmartDevice {
    private boolean isBrewing;

    public SmartCoffeeMaker(String name) {
        super(name, 8);
        isBrewing = false;
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(getName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 128, 255));
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton brewButton = new JButton("Brew Coffee");
        brewButton.setBackground(new Color(245, 245, 245));
        panel.add(brewButton, gbc);

        gbc.gridx++;
        JButton stopButton = new JButton("Stop Brewing");
        stopButton.setBackground(new Color(245, 245, 245));
        panel.add(stopButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JComboBox<String> strengthBox = new JComboBox<>(new String[]{"Mild", "Medium", "Strong"});
        panel.add(strengthBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JButton cleanButton = new JButton("Clean Coffee Maker");
        cleanButton.setBackground(new Color(0, 120, 215));
        cleanButton.setForeground(Color.WHITE);
        panel.add(cleanButton, gbc);

        brewButton.addActionListener(e -> {
            isBrewing = true;
            addEnergyConsumption(2); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " started brewing coffee");
            JOptionPane.showMessageDialog(null, getName() + " started brewing coffee", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        stopButton.addActionListener(e -> {
            isBrewing = false;
            addEnergyConsumption(-2); // Simulate energy consumption
            SmartHomeAutomationSystem.logHistory(getName() + " stopped brewing coffee");
            JOptionPane.showMessageDialog(null, getName() + " stopped brewing coffee", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        strengthBox.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " coffee strength set to " + strengthBox.getSelectedItem());
            JOptionPane.showMessageDialog(null, getName() + " coffee strength set to " + strengthBox.getSelectedItem(), "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });
        cleanButton.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " cleaning started");
            JOptionPane.showMessageDialog(null, getName() + " cleaning started", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            isBrewing = false; // Stop brewing
            SmartHomeAutomationSystem.logHistory(getName() + " turned off for vacation.");
        } else {
            isBrewing = false; // Ensure it's off
            SmartHomeAutomationSystem.logHistory(getName() + " reset after vacation.");
        }
    }
}

class SmartFridge extends SmartDevice {
    private int temperature;

    public SmartFridge(String name) {
        super(name, 10);
        temperature = 35;
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(getName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 128, 255));
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel tempLabel = new JLabel("Temperature:");
        panel.add(tempLabel, gbc);

        gbc.gridx++;
        JSlider tempSlider = new JSlider(30, 40, temperature);
        tempSlider.setPaintTicks(true);
        tempSlider.setMajorTickSpacing(2);
        tempSlider.setMinorTickSpacing(1);
        tempSlider.setPaintLabels(true);
        panel.add(tempSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton defrostButton = new JButton("Defrost");
        defrostButton.setBackground(new Color(0, 120, 215));
        defrostButton.setForeground(Color.WHITE);
        panel.add(defrostButton, gbc);

        tempSlider.addChangeListener(e -> {
            temperature = tempSlider.getValue();
            SmartHomeAutomationSystem.logHistory(getName() + " temperature set to " + temperature + "°F");
            JOptionPane.showMessageDialog(null, getName() + " temperature set to " + temperature + "°F", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        defrostButton.addActionListener(e -> {
            SmartHomeAutomationSystem.logHistory(getName() + " defrost cycle started");
            JOptionPane.showMessageDialog(null, getName() + " defrost cycle started", "Action Info", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    @Override
    public void setVacationMode(boolean isActive) {
        if (isActive) {
            temperature = 40; // Set to higher temperature to save energy
            SmartHomeAutomationSystem.logHistory(getName() + " set to energy saving mode for vacation.");
        } else {
            temperature = 35; // Reset to normal temperature
            SmartHomeAutomationSystem.logHistory(getName() + " reset to normal mode.");
        }
    }
}

class WeatherAdaptation {
    public static void adjustDevicesBasedOnWeather(List<SmartDevice> devices) {
        String weather = getWeatherForecast();
        for (SmartDevice device : devices) {
            if (device instanceof Thermostat) {
                if (weather.contains("cold")) {
                    ((Thermostat) device).setVacationMode(false);
                } else if (weather.contains("hot")) {
                    ((Thermostat) device).setVacationMode(true);
                }
                SmartHomeAutomationSystem.logHistory(device.getName() + " adjusted based on weather: " + weather);
                JOptionPane.showMessageDialog(null, device.getName() + " adjusted based on weather: " + weather, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(null, "Devices adjusted based on weather: " + weather);
    }

    private static String getWeatherForecast() {
        // Mock weather forecast
        String[] weatherConditions = {"sunny", "cloudy", "rainy", "cold", "hot"};
        return weatherConditions[new Random().nextInt(weatherConditions.length)];
    }
}

class VacationMode {
    public static void activate(List<SmartDevice> devices) {
        SmartHomeAutomationSystem.logHistory("Vacation Mode activated");
        for (SmartDevice device : devices) {
            device.setVacationMode(true);
        }
        JOptionPane.showMessageDialog(null, "Vacation Mode activated. Lights will simulate presence, thermostat set to energy-saving mode, and security cameras are activated.");
    }

    public static void deactivate(List<SmartDevice> devices) {
        SmartHomeAutomationSystem.logHistory("Vacation Mode deactivated");
        for (SmartDevice device : devices) {
            device.setVacationMode(false);
        }
        JOptionPane.showMessageDialog(null, "Vacation Mode deactivated. Devices have returned to their normal settings.");
    }
}

class AnomalyDetector {
    private final List<SmartDevice> devices;
    private final Timer anomalyTimer;

    public AnomalyDetector(List<SmartDevice> devices) {
        this.devices = devices;
        this.anomalyTimer = new Timer(true);
        scheduleAnomalyDetection();
    }

    private void scheduleAnomalyDetection() {
        anomalyTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                detectAnomalies();
            }
        }, 0, 60000); // Check every minute
    }

    private void detectAnomalies() {
        for (SmartDevice device : devices) {
            int energyConsumption = device.getEnergyConsumption();
            if (energyConsumption > 100) { // Example anomaly condition
                SmartHomeAutomationSystem.logHistory("Anomaly detected in " + device.getName() + ": High energy consumption (" + energyConsumption + " watts)");
                JOptionPane.showMessageDialog(null, "Anomaly detected in " + device.getName() + ": High energy consumption (" + energyConsumption + " watts)", "Anomaly Detection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}

public class SmartHomeAutomationSystem {
    private final JFrame frame;
    private final JPanel mainPanel;
    private final List<SmartDevice> devices;
    private final List<String> scheduledTasks;
    private final List<String> definedRules;
    private static final JTextArea historyLog = new JTextArea(5, 40);
    private static final String username = "admin";
    private static final String password = "password";

    public SmartHomeAutomationSystem() {
        setUIFont(new FontUIResource(new Font("Arial", Font.PLAIN, 14)));

        if (!authenticateUser()) {
            JOptionPane.showMessageDialog(null, "Authentication failed. Exiting...");
            System.exit(0);
        }

        devices = new ArrayList<>();
        devices.add(new Light("Living Room Light"));
        devices.add(new Thermostat("Bedroom Thermostat"));
        devices.add(new SecurityCamera("Front Door Camera"));
        devices.add(new SmartSpeaker("Living Room Speaker"));
        devices.add(new SmartWindowBlinds("Bedroom Window Blinds"));
        devices.add(new SmartCoffeeMaker("Kitchen Coffee Maker"));
        devices.add(new SmartOven("Kitchen Oven"));
        devices.add(new SmartFridge("Kitchen Fridge"));
        scheduledTasks = new ArrayList<>();
        definedRules = new ArrayList<>();

        frame = new JFrame("Smart Home Automation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800); // Set a specific size instead of full screen
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        frame.setJMenuBar(createMenuBar());
        mainPanel = createMainPanel();
        frame.add(mainPanel, BorderLayout.CENTER);

        new AnomalyDetector(devices); // Start anomaly detection

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartHomeAutomationSystem::new);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JMenu automationMenu = new JMenu("Automation");
        JMenuItem scheduleMenuItem = new JMenuItem("Schedule Task");
        JMenuItem rulesMenuItem = new JMenuItem("Define Rules");
        JMenuItem viewSchedulesMenuItem = new JMenuItem("View Schedules");
        JMenuItem viewRulesMenuItem = new JMenuItem("View Rules");
        JMenuItem vacationModeMenuItem = new JMenuItem("Activate Vacation Mode");
        JMenuItem deactivateVacationModeMenuItem = new JMenuItem("Deactivate Vacation Mode");

        scheduleMenuItem.addActionListener(e -> showScheduleDialog());
        rulesMenuItem.addActionListener(e -> showRulesDialog());
        viewSchedulesMenuItem.addActionListener(e -> showScheduledTasks());
        viewRulesMenuItem.addActionListener(e -> showDefinedRules());
        vacationModeMenuItem.addActionListener(e -> VacationMode.activate(devices));
        deactivateVacationModeMenuItem.addActionListener(e -> VacationMode.deactivate(devices));

        automationMenu.add(scheduleMenuItem);
        automationMenu.add(rulesMenuItem);
        automationMenu.add(viewSchedulesMenuItem);
        automationMenu.add(viewRulesMenuItem);
        automationMenu.add(vacationModeMenuItem);
        automationMenu.add(deactivateVacationModeMenuItem);

        JMenu energyMenu = new JMenu("Energy");
        JMenuItem viewEnergyConsumptionMenuItem = new JMenuItem("View Energy Consumption");

        viewEnergyConsumptionMenuItem.addActionListener(e -> showEnergyConsumption());

        energyMenu.add(viewEnergyConsumptionMenuItem);

        JMenu weatherMenu = new JMenu("Weather");
        JMenuItem adjustBasedOnWeatherMenuItem = new JMenuItem("Adjust Devices Based on Weather");

        adjustBasedOnWeatherMenuItem.addActionListener(e -> WeatherAdaptation.adjustDevicesBasedOnWeather(devices));

        weatherMenu.add(adjustBasedOnWeatherMenuItem);

        JMenu historyMenu = new JMenu("History");
        JMenuItem viewHistoryLogMenuItem = new JMenuItem("View History Log");
        JMenuItem connectDevicesMenuItem = new JMenuItem("Connect Devices");

        viewHistoryLogMenuItem.addActionListener(e -> showHistoryLog());
        connectDevicesMenuItem.addActionListener(e -> showConnectDevicesDialog());

        historyMenu.add(viewHistoryLogMenuItem);
        historyMenu.add(connectDevicesMenuItem);

        menuBar.add(automationMenu);
        menuBar.add(energyMenu);
        menuBar.add(weatherMenu);
        menuBar.add(historyMenu);

        return menuBar;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(255, 255, 255));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel houseLayoutPanel = createHouseLayoutPanel();
        tabbedPane.addTab("House Layout", houseLayoutPanel);

        JPanel uniqueFeaturesPanel = createUniqueFeaturesPanel();
        tabbedPane.addTab("Unique Features", uniqueFeaturesPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHouseLayoutPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10)); // Grid layout for easy organization
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        for (SmartDevice device : devices) {
            JPanel controlPanel = device.getControlPanel();
            panel.add(controlPanel);
        }

        return panel;
    }

    private JPanel createUniqueFeaturesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        JButton viewCCTVButton = new JButton("View CCTV Records");
        viewCCTVButton.setBackground(new Color(0, 120, 215));
        viewCCTVButton.setForeground(Color.WHITE);
        viewCCTVButton.setFont(new Font("Arial", Font.PLAIN, 14));
        viewCCTVButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Displaying CCTV Records...", "CCTV Records", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton manageDeviceSettingsButton = new JButton("Manage Device Settings");
        manageDeviceSettingsButton.setBackground(new Color(0, 120, 215));
        manageDeviceSettingsButton.setForeground(Color.WHITE);
        manageDeviceSettingsButton.setFont(new Font("Arial", Font.PLAIN, 14));
        manageDeviceSettingsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Opening Device Settings Management...", "Device Settings", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(viewCCTVButton, gbc);
        gbc.gridy++;
        panel.add(manageDeviceSettingsButton, gbc);

        return panel;
    }

    private void showScheduleDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        String[] tasks = {"Turn on Lights", "Adjust Thermostat", "Activate Camera", "Play Music", "Open Blinds", "Brew Coffee"};
        JComboBox<String> taskComboBox = new JComboBox<>(tasks);
        String[] times = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        JComboBox<String> timeComboBox = new JComboBox<>(times);

        panel.add(new JLabel("Select a task to schedule:"));
        panel.add(taskComboBox);
        panel.add(new JLabel("Select time:"));
        panel.add(timeComboBox);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Schedule Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String task = (String) taskComboBox.getSelectedItem();
            String time = (String) timeComboBox.getSelectedItem();
            scheduledTasks.add(time + " - " + task);
            logHistory("Scheduled task at " + time + ": " + task);
            JOptionPane.showMessageDialog(frame, "Task scheduled successfully!");
        }
    }

    private void showRulesDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox tempCheckBox = new JCheckBox("Temperature should not exceed 75°F");
        JCheckBox lightCheckBox = new JCheckBox("Turn off all lights at 10 PM");
        JCheckBox cameraCheckBox = new JCheckBox("Activate security camera at night");
        JCheckBox coffeeCheckBox = new JCheckBox("Brew coffee at 7 AM");

        panel.add(tempCheckBox);
        panel.add(lightCheckBox);
        panel.add(cameraCheckBox);
        panel.add(coffeeCheckBox);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Define Rules", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (tempCheckBox.isSelected()) {
                definedRules.add("Temperature should not exceed 75°F");
                logHistory("Defined rule: Temperature should not exceed 75°F");
            }
            if (lightCheckBox.isSelected()) {
                definedRules.add("Turn off all lights at 10 PM");
                logHistory("Defined rule: Turn off all lights at 10 PM");
            }
            if (cameraCheckBox.isSelected()) {
                definedRules.add("Activate security camera at night");
                logHistory("Defined rule: Activate security camera at night");
            }
            if (coffeeCheckBox.isSelected()) {
                definedRules.add("Brew coffee at 7 AM");
                logHistory("Defined rule: Brew coffee at 7 AM");
            }
            JOptionPane.showMessageDialog(frame, "Rules defined successfully!");
        }
    }

    private void showScheduledTasks() {
        JTextArea textArea = new JTextArea(10, 30);
        for (String task : scheduledTasks) {
            textArea.append(task + "\n");
        }
        JOptionPane.showMessageDialog(frame, new JScrollPane(textArea), "Scheduled Tasks", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDefinedRules() {
        JTextArea textArea = new JTextArea(10, 30);
        for (String rule : definedRules) {
            textArea.append(rule + "\n");
        }
        JOptionPane.showMessageDialog(frame, new JScrollPane(textArea), "Defined Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEnergyConsumption() {
        JTextArea textArea = new JTextArea(10, 30);
        int totalEnergy = 0;
        for (SmartDevice device : devices) {
            textArea.append(device.getName() + ": " + device.getEnergyConsumption() + " watts\n");
            totalEnergy += device.getEnergyConsumption();
        }
        textArea.append("Total Energy Consumption: " + totalEnergy + " watts\n");
        JOptionPane.showMessageDialog(frame, new JScrollPane(textArea), "Energy Consumption", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHistoryLog() {
        JOptionPane.showMessageDialog(frame, new JScrollPane(historyLog), "History Log", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showConnectDevicesDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox lightCheckBox = new JCheckBox("Connect Living Room Light");
        JCheckBox thermostatCheckBox = new JCheckBox("Connect Bedroom Thermostat");
        JCheckBox cameraCheckBox = new JCheckBox("Connect Front Door Camera");
        JCheckBox speakerCheckBox = new JCheckBox("Connect Living Room Speaker");
        JCheckBox blindsCheckBox = new JCheckBox("Connect Bedroom Window Blinds");
        JCheckBox coffeeMakerCheckBox = new JCheckBox("Connect Kitchen Coffee Maker");

        panel.add(lightCheckBox);
        panel.add(thermostatCheckBox);
        panel.add(cameraCheckBox);
        panel.add(speakerCheckBox);
        panel.add(blindsCheckBox);
        panel.add(coffeeMakerCheckBox);

        JPanel connectionPanel = new JPanel(new GridLayout(0, 1));
        JLabel connectionLabel = new JLabel("Select Connection Type:");
        JComboBox<String> connectionComboBox = new JComboBox<>(new String[]{"Wi-Fi", "Bluetooth", "Zigbee", "Z-Wave"});
        connectionPanel.add(connectionLabel);
        connectionPanel.add(connectionComboBox);

        int result = JOptionPane.showConfirmDialog(frame, new Object[]{panel, connectionPanel}, "Connect Devices", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String connectionType = (String) connectionComboBox.getSelectedItem();
            if (lightCheckBox.isSelected()) {
                logHistory("Connected Living Room Light via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Living Room Light via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            if (thermostatCheckBox.isSelected()) {
                logHistory("Connected Bedroom Thermostat via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Bedroom Thermostat via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            if (cameraCheckBox.isSelected()) {
                logHistory("Connected Front Door Camera via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Front Door Camera via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            if (speakerCheckBox.isSelected()) {
                logHistory("Connected Living Room Speaker via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Living Room Speaker via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            if (blindsCheckBox.isSelected()) {
                logHistory("Connected Bedroom Window Blinds via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Bedroom Window Blinds via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            if (coffeeMakerCheckBox.isSelected()) {
                logHistory("Connected Kitchen Coffee Maker via " + connectionType);
                JOptionPane.showMessageDialog(frame, "Connected Kitchen Coffee Maker via " + connectionType, "Action Info", JOptionPane.INFORMATION_MESSAGE);
            }
            JOptionPane.showMessageDialog(frame, "Devices connected successfully via " + connectionType + "!");
        }
    }

    public static void logHistory(String action) {
        historyLog.append(action + "\n");
    }

    private boolean authenticateUser() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "User Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return username.equals(usernameField.getText()) && password.equals(new String(passwordField.getPassword()));
        } else {
            return false;
        }
    }

    private void setUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
}
