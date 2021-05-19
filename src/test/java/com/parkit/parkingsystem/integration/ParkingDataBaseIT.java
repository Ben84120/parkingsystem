package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
    private static void setUp() throws Exception{
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
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
    	// when(inputReaderUtil.readSelection()).thenReturn(1);  
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        Ticket savingTicket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
        Assertions.assertNotEquals(null, savingTicket);
        Assertions.assertEquals("ABCDEF", savingTicket.getVehicleRegNumber());
       
        
        ParkingSpot placeParking = parkingSpotDAO.getParkingSpot(1);
        Assertions.assertEquals(false, placeParking.isAvailable());
        
        
        
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }
    
    @Test
	public void testParkingLotExit() throws Exception {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		// TODO: check that the fare generated and out time are populated correctly in
		Thread.sleep(60 * 1000);

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		Assertions.assertTrue(ticket.getOutTime() != null);
		assertThat(ticket.getOutTime()).isAfter(ticket.getInTime());
		long duration =ticket.getOutTime().getTime() - ticket.getInTime().getTime();
		assertThat(duration).isGreaterThanOrEqualTo(60 * 1000);
		assertThat(ticket.getPrice()).isEqualTo(0.0);
		
	}

    @Disabled 
    @Test
    public void testParkingLotExit_carWithLessThanOneHourParkingTime() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		// TODO: check that the fare generated and out time are populated correctly in
		try {
			Thread.sleep(40 *60*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parkingService.processExitingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		Assertions.assertTrue(ticket.getOutTime() != null);
		Assertions.assertEquals(0.0, ticket.getPrice());
	}

    
    @Test
	public void TestParkingACarWhenThreePlaceAreAlreadyTaken() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();
		parkingService.processIncomingVehicle();

		Assertions.assertEquals(0, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}



@Test
public void testParkingABike(){
	 
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    parkingService.processIncomingVehicle();
    
    Ticket savingTicket = ticketDAO.getTicket("ABCDEF");
    Assertions.assertEquals(4, parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE));
    Assertions.assertNotEquals(null, savingTicket);
    Assertions.assertEquals("ABCDEF", savingTicket.getVehicleRegNumber());
   
    
    ParkingSpot placeParking = parkingSpotDAO.getParkingSpot(1);
    Assertions.assertEquals(false, placeParking.isAvailable());
}

@Test
public void TestParkingExitABike() throws Exception {
	

	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingService.processIncomingVehicle();

	Thread.sleep(1000);// Patiente une seconde avant de sortir

	parkingService.processExitingVehicle();
	Ticket ticket = ticketDAO.getTicket("ABCDEF");
	Assertions.assertTrue(ticket.getOutTime() != null);
	Assertions.assertEquals(0.0, ticket.getPrice());
}
    
    
    
    //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
}
