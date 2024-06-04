package org.mtransit.parser.ca_milton_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GStopTime;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

// https://milton.tmix.se/gtfs/gtfs-milton.zip
public class MiltonTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new MiltonTransitBusAgencyTools().start(args);
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Milton Transit";
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if ("not in service".equalsIgnoreCase(gTrip.getTripHeadsign())) {
			return true;
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	private static final String ROUTE_COLOR_SCHOOL = "FFD800"; // School bus yellow

	@Nullable
	@Override
	public String fixColor(@Nullable String color) {
		if (ColorUtils.BLACK.equalsIgnoreCase(color)) {
			return null;
		}
		return super.fixColor(color);
	}

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final String rlnLC = gRoute.getRouteLongNameOrDefault().toLowerCase(Locale.ENGLISH);
		if (rlnLC.contains("school")) {
			return ROUTE_COLOR_SCHOOL;
		}
		return super.provideMissingRouteColor(gRoute);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^[\\d]+[a-z]? (- )?)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWords());
		routeLongName = STARTS_WITH_RSN.matcher(routeLongName).replaceAll(EMPTY);
		routeLongName = CleanUtils.cleanBounds(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "00615C"; // GREEN (like color on buses)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_MI.matcher(gStopId).replaceAll(EMPTY);
		gStopId = ENDS_WITH_T.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		//noinspection RedundantIfStatement
		if (routeId == 2L) {
			return true;
		}
		return super.directionSplitterEnabled(routeId);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final String REMOVE_END_W_RLN_ = "( %s$)";

	@NotNull
	@Override
	public String cleanStopHeadSign(@NotNull GRoute gRoute, @NotNull GTrip gTrip, @NotNull GStopTime gStopTime, @NotNull String stopHeadsign) {
		final String routeLongName = gRoute.getRouteLongNameOrDefault();
		if (!StringUtils.isEmpty(routeLongName)) {
			stopHeadsign = stopHeadsign.replaceAll(
					String.format(REMOVE_END_W_RLN_, routeLongName),
					EMPTY
			);
		}
		return cleanStopHeadSign(stopHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"NE", "SE", "NW", "SW",
				"GO",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final Pattern STARTS_WITH_MI = Pattern.compile("((^)(mi))", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENDS_WITH_T = Pattern.compile("(t$)", Pattern.CASE_INSENSITIVE);

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		String stopId = cleanStopOriginalId(gStop.getStopId());
		return Integer.parseInt(stopId);
	}
}
