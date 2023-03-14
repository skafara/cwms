package ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;

import data_generator.DataGenerator;
import events.AEvent;
import events.BasketRefillEvent;
import events.CamelWalkEvent;
import events.EventManager;
import events.RequestFulfilledEvent;
import events.RequestReceiveEvent;
import requests.RequestManager;
import simulation.Simulation;

/**
 * This class represents an user interface that is used or controlling the simulation
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky
 */
public class UserInterface {

	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static RequestManager REQUEST_MANAGER = RequestManager.getInstance();
	
	private static final Scanner IN = new Scanner(System.in);
	
	private final UIRequestControls requestControls;
	
	private boolean dataLoaded = false;
	private boolean pause = false;
	
	/**
	 * Constructor for class UserInterface
	 * Just prints some initial info about the app
	 */
	public UserInterface() {
		System.out.println("Camel Warehouse Management System");
		System.out.println("Use command 'help' to list available commands");
		requestControls = new UIRequestControls(this);
	}

	/**
	 * Reads a line of input from the user and processes it
	 */
	public void readAndProcessInput() {
		System.out.print(" $ ");
		String line = IN.nextLine();
		processInput(line);
	}
	
	/**
	 * Processes a line of input
	 * @param input line of input
	 */
	private void processInput(String input) {
		String[] commandArr = input.trim().split(" ");
		
		switch (commandArr[0]) {
			case "load":
				loadData(commandArr);
				break;
			case "start":
				startSimulation(commandArr);
				break;
			case "load_and_start":
				loadData(commandArr);
				startSimulation(new String[1]);
				break;
			case "step": 
				step(commandArr);
				break;
			case "time":
				printTime(commandArr);
				break;
			case "help": 
				help(commandArr);
				break;
			case "exit":
			case "quit":
				System.exit(0);
				break;
			default:
				processInput2(commandArr);
		}
	}
	
	//In case you're wondering why this is split, the reason is pmd :(
	private void processInput2(String[] commandArr) {
		switch (commandArr[0]) {
			case "add_request":
				requestControls.addRequest(commandArr);
				break;
			case "list_requests":
				requestControls.listRequests(commandArr);
				break;
			case "cancel_request":
				requestControls.cancelRequest(commandArr);
				break;
			case "schedule_pause":
				schedulePause(commandArr);
				break;
			case "request_info":
				requestControls.printRequestInfo(commandArr);
				break;
			case "generate":
				generateData(commandArr);
				break;
			case "generate_and_start":
				String filename = generateData(commandArr);
				if(filename == null) {
					return;
				}
				loadData(new String[] {"", filename});
				startSimulation(new String[1]);
				break;
			default:
				System.out.println("Unknown command: " + commandArr[0]);
				System.out.println("Use command 'help' to list available commands");
	
			}
	}

	/**
	 * Uses the DataGenerator and generates a new dataset
	 * @param commandArr array of user input (0 - command)
	 * @return relative path to the generated file
	 */
	private String generateData(String [] commandArr) {
		if(commandArr.length != 1) {
			System.out.println("Invalid arguments");
			return null;
		}
		
		if(dataLoaded) {
			System.out.println("Data already loaded");
			return null;
		}
		
		System.out.println("Generating data...");
		
		int i = 1;
		String filename = "data/generated" + i + ".txt";
		File file = new File(filename);
		while(file.isFile()) {
			i++;
			filename = "data/generated" + i + ".txt";
			file = new File(filename);
		}
		
		DataGenerator.generateData(file);
		
		System.out.println("Data succesfully generated into file: " + filename.substring(5));
		return filename.substring(5);
	}

	/**
	 * Prints the current simulation time
	 * @param commandArr array of user input (0 - command)
	 */
	private void printTime(String[] commandArr) {
		if(commandArr.length != 1) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(!dataLoaded) {
			System.out.println("No data loaded");
			return;
		}
		
		System.out.printf("Simulation time: %.3f\n", EVENT_MANAGER.getSimulationTime());
	}

	/**
	 * Schedules a pause for a given time
	 * @param commandArr array of user input (0 - command, 1 - time to schedule the pause for)
	 */
	private void schedulePause(String[] commandArr) {
		if(commandArr.length != 2) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(!dataLoaded) {
			System.out.println("No data loaded");
			return;
		}
		
		double pauseTime = -1;
		try {
			pauseTime = Double.parseDouble(commandArr[1]);
		} catch(NumberFormatException e) {
			System.out.println("Pause time not formatted correctly.");
			return;
		}
		
		if(pauseTime < EVENT_MANAGER.getSimulationTime()) {
			System.out.println("Pause time cannot be less than current time");
			return;
		}
		
		
		EVENT_MANAGER.addEvent(new PauseEvent(pauseTime, this));
		System.out.println("Pause for time: " + pauseTime + " scheduled.");
	}

	
	/**
	 * Loads data into the simulation
	 * @param commandArr array of user input (0 - command, 1 - name of the file)
	 */
	private void loadData(String[] commandArr) {
		if(commandArr.length != 2) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(dataLoaded) {
			System.out.println("Data already loaded");
			return;
		}
		
		String filename = commandArr[1];
		if(!filename.endsWith(".txt")) {
			filename += ".txt";
		}
		filename = "data/" + filename;
		
		System.out.println("Loading " + commandArr[1] + "...");
		try {
			Simulation.loadData(filename);
			System.out.println(commandArr[1] + " succesfuly loaded.");
			dataLoaded = true;
		} catch (NoSuchFileException e) {
			System.out.println("File: " + filename + " does not exist");
		} catch (IOException e) {
			System.out.println("Failed to load the data into the simulation.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the simulation
	 * @param commandArr array of user input (0 - command)
	 */
	private void startSimulation(String[] commandArr) {
		if(commandArr.length != 1) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(!dataLoaded) {
			System.out.println("No data loaded");
			return;
		}
		
		//Test if user cancelled all requests
		REQUEST_MANAGER.testSimulationEnd();
		
		Simulation.startTime = System.nanoTime();
		
		pause = false;
		while(!pause) {
			EVENT_MANAGER.nextEvent();
		}
	}
	
	/**
	 * Displays available commands
	 * @param commandArr array of user input (0 - command)
	 */
	private void help(String[] commandArr) {
		if(commandArr.length != 1) {
			System.out.println("Invalid arguments");
			return;
		}
		
		String format = " %-40s %s\n";
		System.out.println("Available commands: ");
		System.out.printf(format, "help", "displays this help page");
		System.out.printf(format, "load <filename>", "load data into the simulation");
		System.out.printf(format, "start", "starts the simulation");
		System.out.printf(format, "load_and_start <filename>", "loads the simulation and starts it");
		System.out.printf(format, "step [<steps>]", "steps the simulation by 1/<steps> steps");
		System.out.printf(format, "time", "displays the current simulation time");
		System.out.printf(format, "list_requests [<state>]", "prints all requests (with state <state>) and their indices");
		System.out.printf(format, "add_request <request_time> <oasis_index> <basket_count> <delivery_time>\n", "\tadds a request");
		System.out.printf(format, "cancel_request <request_index>", "cancels request with index <request_index>");
		System.out.printf(format, "request_info <request_index>", "prints information about request with index <request_index>"); 
		System.out.printf(format, "schedule_pause <pause_time>", "schedules a pause for time <pause_time>");
		System.out.printf(format, "generate", "generates a new dataset");
		System.out.printf(format, "generate_and_start", "generates a new dataset, loads it and starts it");
		System.out.printf(format, "quit/exit", "ends the app");
	}

	/**
	 * Steps simulation by 1 / multiple steps
	 * @param commandArr array of user input (0 - command, [1 - number of steps])
	 */
	private void step(String[] commandArr) {
		if(commandArr.length > 2) {
			System.out.println("Invalid arguments");
			return;
		}
				
		if(!dataLoaded) {
			System.out.println("No data loaded");
			return;
		}
		
		int stepCount = 1;
		if(commandArr.length == 2) {
			try {
				stepCount = Integer.parseInt(commandArr[1]);
				if(stepCount < 0) {
					System.out.println("Step count cannot be negative");
					return;
				}
			} catch (Exception e) {
				System.out.println("Step count not formatted correctly");
				return;
			}
		} 
			
		step(stepCount);
	}
	
	//Split because of pmd
	private void step(int stepCount) {
		//Test if user cancelled all requests
		REQUEST_MANAGER.testSimulationEnd();
		
		for(int i = 0; i < stepCount; i++) {
			AEvent e = EVENT_MANAGER.nextEvent();

			//Skip events that do not print anything
			while((e instanceof BasketRefillEvent)
					|| (e instanceof CamelWalkEvent && !((CamelWalkEvent)e).isLog())
					|| (e instanceof RequestFulfilledEvent)
					|| (e instanceof RequestReceiveEvent)) {
				e = EVENT_MANAGER.nextEvent();
			}
		}
	}
	

	/**
	 * Pauses the simulation when done processing current event
	 */
	public void pause() {
		pause = true;
	}
	
	/**
	 * Method to check whether data has been loaded into the simulation
	 * @return the dataLoaded
	 */
	public boolean isDataLoaded() {
		return dataLoaded;
	}
}