-- Optional demo reset. This intentionally removes only booking-related demo data.
USE cineticket_desktop;
START TRANSACTION;
DELETE FROM etickets;
DELETE FROM payments;
DELETE FROM tickets;
DELETE FROM bookings;
UPDATE showtime_seats SET status='AVAILABLE', held_until=NULL, version=version+1;
COMMIT;
