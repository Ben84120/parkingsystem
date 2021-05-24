package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		float duration = (float) (outHour - inHour) / 3600000;

		if (duration < 0.5) { // calculation of free ticket below 30 minutes of parking
			ticket.setPrice(duration * Fare.CAR_BIKE_FREE);
			System.out.println("Votre ticket est gratuit");
			return;
		}

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
			break;
		}
		case BIKE: {
			ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
			break;

		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");

		}

		if (ticket.getRecurring()) {
			ticket.setPrice(ticket.getPrice() * 0.95);
			System.out.println("Votre remise de 5% est appliquée " + ticket.getPrice());

		}

	}

}
