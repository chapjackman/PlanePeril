package rem;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import scn.MultiplayerLeft;
import scn.MultiplayerRight;

import cls.Aircraft;
import cls.FlightPlan;
import cls.Waypoint;

public class MultiplayerServer extends UnicastRemoteObject implements MultiplayerInterface {
	String opponent_address;
	Registry registry;
	MultiplayerInterface multiplayer_interface;
	Registry their_registry;
	int opponent_port;
	
	MultiplayerLeft left_game;
	MultiplayerRight right_game;
	
	public ArrayList<Aircraft> aircraft_queue = new ArrayList<Aircraft>();
	
	boolean left;

	public MultiplayerServer(MultiplayerLeft game_screen, String opponent_address, String registry_name, int my_port, int opponent_port) throws RemoteException {
		super();
		left = true;
		left_game = game_screen;
		
		registry = LocateRegistry.createRegistry(my_port);
		registry.rebind(registry_name, this);
		this.opponent_address = opponent_address;
		this.opponent_port = opponent_port;
	}
	
	public MultiplayerServer(MultiplayerRight game_screen, String opponent_address, String registry_name, int my_port, int opponent_port) throws RemoteException {
		super();
		left = false;
		right_game = game_screen;
		
		registry = LocateRegistry.createRegistry(my_port);
		registry.rebind(registry_name, this);
		this.opponent_address = opponent_address;
		this.opponent_port = opponent_port;
	}
	
	// Initial connection
	
	public void connect(String registry_name) {
		try {
			their_registry = LocateRegistry.getRegistry(opponent_address, opponent_port);
			multiplayer_interface = (MultiplayerInterface)(their_registry.lookup(registry_name));
		} catch (NotBoundException | RemoteException e) {
			e.printStackTrace();
		}
	}
	
	// Notifying each game of a new aircraft
	
	// Server sending an aircraft
	public void sendAircraft(boolean from_airport, int speed, int origin_waypoints_index, int destination_waypoints_index, int preferred_altitude_index) {
		try {
			multiplayer_interface.addAircraft(from_airport, speed, origin_waypoints_index, destination_waypoints_index, preferred_altitude_index);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	// Receiving aircraft from other server
	@Override
	public void addAircraft(boolean from_airport, int speed, int origin_waypoints_index, int destination_waypoints_index, int preferred_altitude_index) {

		String name = "Flight " + (int)(900 * Math.random() + 100);
			
		if (left) {
			Waypoint origin_point;
			if (from_airport) {
				origin_point = left_game.right_airport; 
			} else {
				origin_point = left_game.right_entryexit_waypoints[origin_waypoints_index];
			}
			
			Waypoint destination_point = left_game.right_entryexit_waypoints[destination_waypoints_index];
			
			aircraft_queue.add(new Aircraft(name, left_game.aircraft_image, speed, 1, new FlightPlan(origin_point, 
					destination_point, left_game.right_waypoints, left_game.right_holding_waypoints, left_game.right_airport_takeoff_waypoint), preferred_altitude_index));
		} else {
			Waypoint origin_point;
			if (from_airport) {
				origin_point = right_game.left_airport; 
			} else {
				origin_point = right_game.left_entryexit_waypoints[origin_waypoints_index];
			}
			
			Waypoint destination_point = right_game.left_entryexit_waypoints[destination_waypoints_index];
			
			//TODO improve synchronisation methods
			aircraft_queue.add(new Aircraft(name, right_game.aircraft_image, speed, 1, new FlightPlan(origin_point, 
					destination_point, right_game.left_waypoints, right_game.left_holding_waypoints, right_game.left_airport_takeoff_waypoint), preferred_altitude_index));
		}
	}
	
}