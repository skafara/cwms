package simulation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import camels.Camel;
import events.EventManager;
import requests.Request;
import requests.RequestManager;

/**
 * Represents a class taking care of simulation statistics.
 * 
 * @author Stanislav Kafara
 * @version 1 01-12-2022
 */
public class Statistics {
	
	/** Whether statistics should be generated */
	private static final boolean GENERATE_STATISTICS = true;
	
	private static final String STATISTICS_DIR = "statistics";

	private static final Statistics INSTANCE = new Statistics();
	
	private static simulation.Map MAP = simulation.Map.getInstance();
	private static EventManager EVENT_MANAGER = EventManager.getInstance();
	private static RequestManager REQUEST_MANAGER = RequestManager.getInstance();
	
	private final Map<Camel, Collection<CamelDelivery>> deliveries;
	
	private final Map<Warehouse, List<WarehouseRefill>> refills;
	
	private Statistics() {
		this.deliveries = new HashMap<>();
		this.refills = new HashMap<>();
	}
	
	/**
	 * Returns the singleton.
	 * @return Singleton.
	 */
	public static Statistics getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Adds a camel delivery record.
	 * @param camel Camel.
	 * @param request Request.
	 * @param load Number of carried baskets.
	 * @param timeDepart Time of departure.
	 * @param timeDeliver Time of delivery.
	 * @param timeReturn Time of return to home warehouse.
	 * @param path Path the camel follows.
	 * @param drinking Camel drinking records.
	 */
	public void addCamelDelivery(Camel camel, Request request, int load, double timeDepart, double timeDeliver, double timeReturn, List<Integer> path, List<Drinking> drinking) {
		if(GENERATE_STATISTICS) {
			if (!deliveries.containsKey(camel)) {
				deliveries.put(camel, new ArrayList<>());
			}
			deliveries.get(camel).add(new CamelDelivery(request, load, timeDepart, timeDeliver, timeReturn, path, drinking));
		}
	}
	
	/**
	 * Creates a record of camel drinking.
	 * @param time Time.
	 * @param nodeIndex Node index.
	 * @return New record of camel drinking.
	 */
	public Drinking createDrinkingRecord(double time, int nodeIndex) {
		return new Drinking(time, nodeIndex);
	}
	
	/**
	 * Adds a warehouse refill record.
	 * @param warehouse Warehouse.
	 * @param time Time of refilling.
	 * @param basketsBefore Number of baskets before refilling.
	 */
	public void addWarehouseRefill(Warehouse warehouse, double time, int basketsBefore) {
		if(GENERATE_STATISTICS) {
			if (!refills.containsKey(warehouse)) {
				refills.put(warehouse, new ArrayList<>());
			}
			refills.get(warehouse).add(new WarehouseRefill(time, basketsBefore));
		}
	}
	
	/**
	 * Appends an error message after the simulation statistics.
	 * @param s Error message.
	 */
	public void appendErrorMessage(String s) {
		if(GENERATE_STATISTICS) {
			try (BufferedWriter bfw = Files.newBufferedWriter(Paths.get(STATISTICS_DIR + "/simulation.txt"), StandardOpenOption.APPEND)) {
				bfw.write("[Chyba] ");
				bfw.write(s);
				bfw.newLine();
			}
			catch (IOException e) {
				System.err.print("Pri zapisovani chybove zpravy doslo k neocekavane chybe pri praci se soubory.");
			}
		}
	}
	
	/**
	 * Generates the simulation statistics.
	 */
	public void generateStatistics() {
		if(GENERATE_STATISTICS) {
			try {
				if (!Files.exists(Paths.get(STATISTICS_DIR))) {
					Files.createDirectory(Paths.get(STATISTICS_DIR));
				}
				generateCamelsStatistics();
				generateOasesStatistics();
				generateWarehousesStatistics();
				generateSimulationStatistics();
			}
			catch (IOException e) {
				System.err.println("Pri generovani statistik doslo k neocekavane chybe pri praci se soubory.");
			}
		}
	}

	private void generateCamelsStatistics() throws IOException {
		try (BufferedWriter bfw = Files.newBufferedWriter(Paths.get(STATISTICS_DIR + "/camels.txt"))) {
			Warehouse[] warehouses = MAP.getWarehouses();
			for (Warehouse warehouse : warehouses) {
				List<Camel> warehouseCamels = new ArrayList<>(warehouse.getOwnedCamels());
				warehouseCamels.sort((c1, c2) -> c1.getIndex() - c2.getIndex());
				for (Camel camel : warehouseCamels) {
					printCamel(bfw, camel);
					bfw.newLine();
				}
			}
		}
	}
	
	private void printCamel(BufferedWriter bfw, Camel c) throws IOException {
		bfw.write(String.format("Velbloud #%d", c.getIndexPlusOne()));
		bfw.newLine();
		bfw.write(String.format("- druh: %s", c.getType().getName()));
		bfw.newLine();
		bfw.write(String.format("- id_sklad: %d", c.getHome().getIndexPlusOne()));
		bfw.newLine();
		bfw.write(String.format("- max_vzdalenost: %.2f", c.getDistance()));
		bfw.newLine();
		bfw.write(String.format("- trasy: %d", (deliveries.containsKey(c) ? deliveries.get(c).size() : 0)));
		bfw.newLine();
		if (deliveries.containsKey(c)) {
			for (CamelDelivery cd : deliveries.get(c)) {
				printCamelDelivery(bfw, cd);
			}
		}
		bfw.write(String.format("- cas_odpocinku: %.2f", getCamelRestTime(c)));
		bfw.newLine();
		bfw.write(String.format("- usla_vzdalenost: %.2f", getCamelTotalWalkedDistance(c)));
		bfw.newLine();
	}
	
	private void printCamelDelivery(BufferedWriter bfw, CamelDelivery cd) throws IOException {
		bfw.write(String.format("  * Trasa #%d", cd.id));
		bfw.newLine();
		bfw.write(String.format("    - cas_odchodu: %.2f", cd.timeDepart));
		bfw.newLine();
		bfw.write(String.format("    - cesta: %s", getCamelDeliveryPathString(cd)));
		bfw.newLine();
		bfw.write(String.format("    - naklad: %d", cd.load));
		bfw.newLine();
		bfw.write(String.format("    - napojeni: %d", cd.drinking.size()));
		bfw.newLine();
		int d_index = 1;
		for (Drinking d : cd.drinking) {
			bfw.write(String.format("      * Napojeni %d", d_index));
			bfw.newLine();
			bfw.write(String.format("        - cas: %.2f", d.time));
			bfw.newLine();
			if (MAP.isOasisIndex(d.nodeIndex)) {
				bfw.write(String.format("        - id_oaza: %d", MAP.nodeToOasisIndex(d.nodeIndex) + 1));
			}
			else {
				bfw.write(String.format("        - id_sklad: %d", d.nodeIndex + 1));
			}
			bfw.newLine();
			d_index++;
			
		}
		bfw.write(String.format("    - id_oazy: %d", cd.request.getOasisIndexPlusOne()));
		bfw.newLine();
		bfw.write(String.format("    - cas_doruceni: %.2f", cd.timeDeliver));
		bfw.newLine();
		bfw.write(String.format("    - cas_navratu: %.2f", cd.timeReturn));
		bfw.newLine();
	}
	
	private String getCamelDeliveryPathString(CamelDelivery cd) {
		List<Integer> path = new ArrayList<>(cd.path);
		for (int p = path.size() - 2; p >= 0; p--) {
			path.add(path.get(p));
		}
		return path.stream()
			   .map(i -> (MAP.isOasisIndex(i)) ?
					   	  "oaza_"+(MAP.nodeToOasisIndex(i)+1)
					   	  : "sklad_"+(i + 1))
			   .collect(Collectors.joining(", "));
	}
	
	private double getCamelRestTime(Camel c) {
		return (deliveries.containsKey(c)) ?
				 EVENT_MANAGER.getSimulationTime()
				 - c.getGenerationTime()
					 - deliveries.get(c).stream()
					 .mapToDouble(cd -> cd.timeReturn - cd.timeDepart)
				 	 .sum()
				 : EVENT_MANAGER.getSimulationTime()
				 - c.getGenerationTime();
	}
	
	private double getCamelTotalWalkedDistance(Camel c) {
		double distance = 0;
		if (deliveries.containsKey(c)) {
			for (CamelDelivery cd : deliveries.get(c)) {
				Iterator<Integer> pathIterator = cd.path.iterator();
				int i = pathIterator.next();
				while (pathIterator.hasNext()) {
					int j = pathIterator.next();
					distance += MAP.getNodeAtIndex(i).getCoords().airDistanceTo(MAP.getNodeAtIndex(j).getCoords());
					j = i;
				}
			}
		}
		return distance;
	}

	private void generateOasesStatistics() throws IOException {
		try (BufferedWriter bfw = Files.newBufferedWriter(Paths.get(STATISTICS_DIR + "/oases.txt"))) {
			Map<Integer, List<Request>> oasisRequests = new TreeMap<>(
					REQUEST_MANAGER.getAllRequests().keySet()
					.stream().collect(Collectors.groupingBy(r -> r.getOasisIndexPlusOne()))
			);
			for (Map.Entry<Integer, List<Request>> entry : oasisRequests.entrySet()) {
				entry.getValue().sort((r1, r2) -> r1.compareTo(r2));
				bfw.write(String.format("Oaza #%d", entry.getKey()));
				bfw.newLine();
				bfw.write(String.format("- pozadavky: %d", entry.getValue().size()));
				bfw.newLine();
				for (Request r : entry.getValue()) {
					printRequest(bfw, r);
					bfw.newLine();
				}
			}
		}
	}

	private void printRequest(BufferedWriter bfw, Request r) throws IOException {
		bfw.write(String.format("  * Pozadavek #%d", r.getIndex() + 1));
		bfw.newLine();
		bfw.write(String.format("    - cas_prichodu: %.2f", r.getRequestTime()));
		bfw.newLine();
		bfw.write(String.format("    - pocet_kosu: %d", r.getBasketCount()));
		bfw.newLine();
		bfw.write(String.format("    - limit_doruceni: %.2f", r.getDeliveryTime()));
		bfw.newLine();
		bfw.write(String.format("    - cas_doruceni: %.2f", r.getDeliveredTime()));
		bfw.newLine();
		bfw.write(String.format("    - obsluha_sklad: %s", r.getServingCamels().stream()
														   .mapToInt(c -> c.getHome().getIndexPlusOne())
														   .distinct()
														   .sorted()
														   .mapToObj(i -> String.valueOf(i))
														   .collect(Collectors.joining(", "))));
		bfw.newLine();
		bfw.write(String.format("    - obsluha_velbloud: %s", r.getServingCamels().stream()
														   .map(c -> String.valueOf(c.getIndexPlusOne()))
														   .collect(Collectors.joining(", "))));
		bfw.newLine();
	}

	private void generateWarehousesStatistics() throws IOException {
		try (BufferedWriter bfw = Files.newBufferedWriter(Paths.get(STATISTICS_DIR + "/warehouses.txt"))) {
			for (Warehouse w : MAP.getWarehouses()) {
				bfw.write(String.format("Sklad #%d", w.getIndexPlusOne()));
				bfw.newLine();
				bfw.write(String.format("- doplneni: %d", (refills.containsKey(w)) ?
														  refills.get(w).size()
														  : 0));
				bfw.newLine();
				if (refills.containsKey(w)) {
					int wr_id = 1;
					for (WarehouseRefill wr : refills.get(w)) {
						bfw.write(String.format("  * Doplneni %d", wr_id));
						bfw.newLine();
						bfw.write(String.format("    - cas: %.2f", wr.time));
						bfw.newLine();
						bfw.write(String.format("    - kosu_pred: %d", wr.basketsBefore));
						bfw.newLine();
						bfw.write(String.format("    - kosu_po: %d", wr.basketsBefore + w.getBasketRefillCount()));
						bfw.newLine();
						wr_id++;
					}
				}
				bfw.newLine();
			}
		}
	}

	private void generateSimulationStatistics() throws IOException {
		try (BufferedWriter bfw = Files.newBufferedWriter(Paths.get(STATISTICS_DIR + "/simulation.txt"))) {
			bfw.write(String.format("trvani: %.2f", EVENT_MANAGER.getSimulationTime()));
			bfw.newLine();
			List<Camel> camels = new ArrayList<>();
			for (Warehouse w : MAP.getWarehouses()) {
				camels.addAll(w.getOwnedCamels());
			}
			bfw.write(String.format(
					"velbloudi_odpocinek_celkem: %.2f",
					camels.stream()
						  .mapToDouble(c -> getCamelRestTime(c))
						  .sum()));
			bfw.newLine();
			bfw.write(String.format("pouziti_velbloudi: %d", Camel.getTotalCamelCount()));
			bfw.newLine();
			Map<String, Long> camelTypeCounts = new TreeMap<>(camels.stream()
													  				.collect(Collectors.groupingBy(
																		 c -> c.getType().getName(),
																		 Collectors.counting())));
			for (Map.Entry<String, Long> entry : camelTypeCounts.entrySet()) {
				bfw.write(String.format("- %s %d", entry.getKey(), entry.getValue()));
				bfw.newLine();
			}
			bfw.newLine();
		}
	}
	
	private static class CamelDelivery {
		
		private static int instanceCounter = 0;
		
		private final int id;
		private final Request request;
		private final int load;
		private final double timeDepart;
		private final double timeDeliver;
		private final double timeReturn;
		private final List<Integer> path;
		private final List<Drinking> drinking;
		
		private CamelDelivery(Request request, int load, double timeDepart, double timeDeliver, double timeReturn, List<Integer> path, List<Drinking> drinking) {
			this.id = ++instanceCounter;
			this.request = request;
			this.load = load;
			this.timeDepart = timeDepart;
			this.timeDeliver = timeDeliver;
			this.timeReturn = timeReturn;
			this.path = path;
			this.drinking = drinking;
		}
		
	}
	
	/**
	 * Represents a record of camel drinking.
	 * @author Stanislav Kafara
	 * @version 1 01-12-22
	 */
	public static class Drinking {

		private final double time;
		private final int nodeIndex;
		
		private Drinking(double time, int nodeIndex) {
			this.time = time;
			this.nodeIndex = nodeIndex;
		}
		
	}
	
	private static class WarehouseRefill {
		
		private final double time;
		private final int basketsBefore;

		private WarehouseRefill(double time, int basketsBefore) {
			this.time = time;
			this.basketsBefore = basketsBefore;
		}
		
	}

}
