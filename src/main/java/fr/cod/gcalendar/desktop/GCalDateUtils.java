package fr.cod.gcalendar.desktop;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.google.api.client.util.DateTime;

/**
 * Classe d'utilitaire de dates. 
 * @author Carl
 *
 */
final class GCalDateUtils {

	static DateTime getDateTime(String paramDate) throws IOException {
		DateTime dateReturned;
		try {

			DateFormat df;
			if (paramDate.length() == 4) {

				df = new SimpleDateFormat("ddMMyyyy", Locale.FRENCH);
				java.util.Calendar cal = new GregorianCalendar(Locale.FRANCE);
				int currentYear = cal.get(java.util.Calendar.YEAR);
				Date dateEstimated = df.parse(paramDate + String.valueOf(currentYear));

				Date now = new Date();
				if (dateEstimated.before(now)) {
					// Date passée, l'année est donc incrémentée
					dateEstimated = df.parse(paramDate + String.valueOf(currentYear + 1));
				}

				dateReturned = new DateTime(dateEstimated);
			} else if (paramDate.length() == 6) {
				df = new SimpleDateFormat("ddMMyy", Locale.FRENCH);
				dateReturned = new DateTime(df.parse(paramDate));
			} else {
				df = new SimpleDateFormat("ddMMyyyy", Locale.FRENCH);
				dateReturned = new DateTime(df.parse(paramDate));
			}

			System.out.println("Date utilisee : " + dateReturned);

		} catch (ParseException e) {
			String message = "La date [" + paramDate + " n'a pas le bon format.";
			System.out.println(message);
			throw new IOException(message, e);
		}
		return dateReturned;
	}

	static Date getDate(String paramDate) throws IllegalArgumentException {
		Date dateReturned;
		try {

			DateFormat df;
			if (paramDate.length() == 4) {

				df = new SimpleDateFormat("ddMMyyyy", Locale.FRENCH);
				java.util.Calendar cal = new GregorianCalendar(Locale.FRANCE);
				int currentYear = cal.get(java.util.Calendar.YEAR);
				dateReturned = df.parse(paramDate + String.valueOf(currentYear));

				Date now = new Date();
				if (dateReturned.before(now)) {
					// Date passée, l'année est donc incrémentée
					dateReturned = df.parse(paramDate + String.valueOf(currentYear + 1));
				}

			} else if (paramDate.length() == 6) {
				df = new SimpleDateFormat("ddMMyy", Locale.FRENCH);
				dateReturned = df.parse(paramDate);
			} else {
				df = new SimpleDateFormat("ddMMyyyy", Locale.FRENCH);
				dateReturned = df.parse(paramDate);
			}

			DateFormat userDf = new SimpleDateFormat("EEEE dd MMMM", Locale.FRENCH);
			System.out.println("Programme du " + userDf.format(dateReturned)+ " :");

		} catch (ParseException e) {
			String message = "Le parametre date [" + paramDate + "] n'a pas le bon format.";
			System.out.println(message);
			throw new IllegalArgumentException(message, e);
		}
		return dateReturned;
	}

	static Date ajouteJours(Date dateReference, int nbJours) {
		int year = Integer.valueOf((new SimpleDateFormat("yyyy", Locale.FRENCH)).format(dateReference));
		int month = Integer.valueOf((new SimpleDateFormat("MM", Locale.FRENCH)).format(dateReference));
		int day = Integer.valueOf((new SimpleDateFormat("dd", Locale.FRENCH)).format(dateReference));

		Calendar cal = new GregorianCalendar(year, month - 1, day, 0, 0, 0);
		cal.add(Calendar.DAY_OF_YEAR, nbJours);
		return cal.getTime();
	}
	
	/**
	 * Compare deux {@link DateTime} uniquement sur les heures et les minutes. <p>
	 * Donc le 1er janvier à 15h est plus grand que le 2 janvier à 9h.
	 * @param start1
	 * @param start2
	 * @return
	 */
	static int compareHeuresMinutes(DateTime start1, DateTime start2) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date(start1.getValue()));
		int minutes1 = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		cal.setTime(new Date(start2.getValue()));
		int minutes2 = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

		// System.out.println(start1+" plus "+(Integer.compare(minutes1, minutes2)<0?
		// "petit":"grand")+" que "+start2);
		return Integer.compare(minutes1, minutes2);
	}
}
