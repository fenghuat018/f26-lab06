package edu.cmu.cs214.booking;

/**
 * Request data for creating one room booking.
 *
 * @param roomId      the room to book, non-null
 * @param startMinute first minute of the booking, inclusive
 * @param endMinute   first minute after the booking, exclusive; must be greater
 *                    than {@code startMinute}
 * @param waitlistKey caller's waitlist key, or null to decline waitlisting
 * @param notes       caller's notes for the booking, or null if none
 */
public record BookingRequest(String roomId, long startMinute, long endMinute,
                             String waitlistKey, String notes) {
}
