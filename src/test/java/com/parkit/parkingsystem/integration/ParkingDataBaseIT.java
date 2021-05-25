package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingA_CarWhenParkingspotIsFree() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		Ticket savingTicket = ticketDAO.getTicket("ABCDEF");
		assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(2);
		assertThat(savingTicket).isNotNull();
		assertThat(savingTicket.getVehicleRegNumber()).isEqualTo("ABCDEF");

		ParkingSpot placeParking = parkingSpotDAO.getParkingSpot(1);
		assertThat(placeParking.isAvailable()).isEqualTo(false);

		
	}

	
	@Test
	public void testParkingLotExit_WithGetOutTime() throws Exception {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		Thread.sleep(6 * 1000);

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertThat(ticket.getOutTime() != null);
		assertThat(ticket.getOutTime()).isAfter(ticket.getInTime());
		long duration = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
		assertThat(duration).isGreaterThanOrEqualTo(6 * 1000);
		assertThat(ticket.getPrice()).isEqualTo(0.0);

	}

	@Disabled
	@Test
	public void testParkingLotExit_carWithLessThanOneHourParkingTime() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		
		try {
			Thread.sleep(40 * 60 * 1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertThat(ticket.getOutTime() != null);
		assertThat(ticket.getPrice()).isEqualTo(0.0);
	}

	@Disabled
	@Test
	public void testParkingLotExit_carWithLessThirtyMinutesParkingTime() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		
		try {
			Thread.sleep(29 * 60 * 1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertThat(ticket.getOutTime() != null);
		assertThat(ticket.getPrice()).isEqualTo(0.0);
	}

	@Disabled
	@Test
	public void testParkingLotExit_carWithMoreThanADaysParkingTime() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		
		try {
			Thread.sleep(24 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertThat(ticket.getOutTime() != null);
		assertThat(ticket.getPrice()).isEqualTo(0.0);
	}

	@Test
	public void TestParking_ThreeCars() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();

		assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(0);
	}
	
	@Test
	public void TestParking_ThreeBikes() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();
		

		assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).isEqualTo(4);
	}

	@Test
	public void testParkingABike_AndSavingTicketInDB() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		Ticket savingTicket = ticketDAO.getTicket("ABCDEF");
		assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).isEqualTo(4);
		assertThat(savingTicket).isNotNull();
		assertThat(savingTicket.getVehicleRegNumber()).isEqualTo("ABCDEF");

		ParkingSpot placeParking = parkingSpotDAO.getParkingSpot(1);
		assertThat(placeParking.isAvailable()).isEqualTo(false);
	}
	
	
	@Test
	public void TestParkingExitABike_WithSavingTicketInDB() throws Exception {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		Thread.sleep(1000);// Patiente une seconde avant de sortir

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket.getOutTime() != null).isTrue();
		assertThat(ticket.getPrice()).isEqualTo(0.0);
	}

	
	@Test
	public void testParkingType_Car() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		assertThat(parkingService.getVehichleType()).isEqualTo(ParkingType.CAR);
	}

	@Test
	public void testParkingType_Bike() {
		when(inputReaderUtil.readSelection()).thenReturn(2);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		assertThat(parkingService.getVehichleType()).isEqualTo(ParkingType.BIKE);
	}

}
