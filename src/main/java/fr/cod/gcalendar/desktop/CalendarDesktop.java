package fr.cod.gcalendar.desktop;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Utilisation de l'API Google Calendar pour récupérer tous les événements d'une date.
 * Fonctionne en tant qu'application java. 
 * Nécessite credentials.json fourni par Google, à placer dans src/main/resources.
 * 
 * @author Carl
 */
public class CalendarDesktop {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarDesktop.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

	public static void main(String... args) throws IllegalArgumentException, IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar gCalendarService = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // à supprimer
		args = new String[] { "1710" };

		if (args != null && args.length == 1) {
			listEventsOnDate(args[0], gCalendarService);
		} else {
			listNext10Events(gCalendarService);
		}
    }

	/**
	 * Affiche les événements d'une journée, pour tous les calendriers. 
	 * @param paramDate journée à afficher
	 * @param gCalendarService
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private static void listEventsOnDate(String paramDate, Calendar gCalendarService)
			throws IllegalArgumentException, IOException {
		
		Date dateParametre = GCalDateUtils.getDate(paramDate);
		DateTime jourSuivant = new DateTime(GCalDateUtils.ajouteJours(dateParametre, 1));		
		
		CalendarList myCalendars = gCalendarService.calendarList().list().execute();
		List<Event> evenements = new LinkedList<Event>();
		for (CalendarListEntry googleCalendar : myCalendars.getItems()) {
			//System.out.println("Calendrier : " + googleCalendar.getSummary());
			String calendarId = googleCalendar.getId();
			Events eventsOneCalendar = gCalendarService.events().list(calendarId)
			        .setTimeMin(new DateTime(dateParametre))
			        .setTimeMax(jourSuivant)
			        .execute();		
			evenements.addAll(eventsOneCalendar.getItems());
			//printEvents(items);
		}
		
		printEventsOnDate(evenements);
	}


	/**
	 * Affiche les 10 prochains événements du calendrier principal.
	 * @param gCalendarService
	 * @throws IOException
	 */
	private static void listNext10Events(Calendar gCalendarService) throws IOException {
		// List the next 10 events from the primary calendar.
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = gCalendarService.events().list("primary")
		        .setMaxResults(10)
		        .setTimeMin(now)
		        .setOrderBy("startTime")
		        .setSingleEvents(true)
		        .execute();
		List<Event> items = events.getItems();
		printEvents(items);
	}

	private static void printEvents(List<Event> events) {
		if (events.isEmpty()) {
		    System.out.println("  No upcoming events found.");
		} else {
		    System.out.println("  Upcoming events");
		    for (Event event : events) {
		        DateTime start = event.getStart().getDateTime();
		        if (start == null) {
		            start = event.getStart().getDate();
		        }
		        
		        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH'H'mm", Locale.FRANCE);
				System.out.printf(Locale.FRANCE, "    %s (%s)\n", event.getSummary(),
						df.format(new Date(start.getValue())));
		    }
		}
	}

	/**
	 * Affiche les événements dans l'ordre chronologique de la journée.
	 * @param events événements d'une seule journée
	 */
	private static void printEventsOnDate(List<Event> events) {
		
		Collections.sort(events, new Comparator<Event>() {
			@Override
			public int compare(Event event1, Event event2) {
				DateTime start1 = event1.getStart().getDateTime();
				DateTime start2 = event2.getStart().getDateTime();
				if (start1 == null || start1.isDateOnly()) {
					// System.out.println("start1 "+(start1==null?"null":"date seule"));
					return -1;
				} else if (start2 == null || start2.isDateOnly()) {
					// System.out.println("start2 "+(start2==null?"null":"date seule"));
					return 1;
				} else {
					return GCalDateUtils.compareHeuresMinutes(start1, start2);
				}
			}
		});
		
		if (!events.isEmpty()) {
		    for (Event event : events) {
		        DateTime start = event.getStart().getDateTime();
		        DateFormat df = new SimpleDateFormat("HH'H'mm", Locale.FRANCE);
				String heureAffichee = start == null ? "" : (df.format(new Date(start.getValue())) + " : ");
		        
				System.out.printf(Locale.FRANCE, "%s%s\n", heureAffichee, event.getSummary());
		    }
		}
	}
}