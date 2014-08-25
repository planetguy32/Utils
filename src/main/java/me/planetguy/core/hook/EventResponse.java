package me.planetguy.core.hook;

public enum EventResponse {
	
	/**
	 * A FORCE_ALLOW response causes the event to happen, regardless of other ALLOW or DENY responses.
	 */
	FORCE_ALLOW,
	/**
	 * An ALLOW response means that, barring any DENY responses, the action will continue.
	 */
	ALLOW,
	/**
	 * A DENY response means that, barring any FORCE_ALLOW responses, the action will continue.
	 */
	DENY

}
