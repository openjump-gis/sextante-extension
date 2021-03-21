
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.openjump.geom.qa.validator;

import es.unex.sextante.core.Sextante;

/**
 * The types of validation errors detected by Validator.
 * @see Validator
 */
public class ValidationErrorType {
	/** Geometry class not allowed */
	public final static ValidationErrorType GEOMETRY_CLASS_DISALLOWED = new ValidationErrorType(
			Sextante.getText("qa.Validator.geometry-class-not-allowed"));

	/** Basic topology is invalid */
	public final static ValidationErrorType BASIC_TOPOLOGY_INVALID = new ValidationErrorType(
			Sextante.getText("qa.Validator.basic-topology-is-invalid"));

	/** Polygon shell is oriented counter-clockwise */
	public final static ValidationErrorType EXTERIOR_RING_CCW = new ValidationErrorType(
			Sextante.getText("qa.Validator.polygon-shell-is-oriented-counter-clockwise"));

	/** Polygon hole is oriented clockwise */
	public final static ValidationErrorType INTERIOR_RING_CW = new ValidationErrorType(
			Sextante.getText("qa.Validator.polygon-hole-is-oriented-clockwise"));

	/** 
	 * Linestring not simple 
	 * @since OpenJUMP 1.6
	 */
	public final static ValidationErrorType NONSIMPLE = new ValidationErrorType(
			Sextante.getText("qa.Validator.non-simple"));

	/** Contains segment with length below minimum */
	public final static ValidationErrorType SMALL_SEGMENT = new ValidationErrorType(
			Sextante.getText("qa.Validator.contains-segment-with-length-below-minimum"));

	/** Is/contains polygon with area below minimum */
	public final static ValidationErrorType SMALL_AREA = new ValidationErrorType(
			Sextante.getText("qa.Validator.is-contain-polygon-with-area-below-minimum"));

	/** Contains segments with angle below minimum */
	public final static ValidationErrorType SMALL_ANGLE = new ValidationErrorType(
			Sextante.getText("qa.Validator.contains-segments-with-angle-below-minimum"));

	/** Polygon has holes */
	public final static ValidationErrorType POLYGON_HAS_HOLES = new ValidationErrorType(
			Sextante.getText("qa.Validator.polygon-has-holes"));

	/** Consecutive points are the same */
	public final static ValidationErrorType REPEATED_CONSECUTIVE_POINTS = new ValidationErrorType(
			Sextante.getText("qa.Validator.consecutive-points-are-the-same"));

	/** LineStrings self overlap */
	public final static ValidationErrorType LINES_SELF_OVERLAP = new ValidationErrorType(
			Sextante.getText("qa.Validator.lines-self-overlap"));

	/** LineStrings self intersect*/
	public final static ValidationErrorType LINES_SELF_INTERSECT = new ValidationErrorType(
			Sextante.getText("qa.Validator.lines-self-intersect"));
	private String message;

	private ValidationErrorType(String message) {
		this.message = message;
	}

	/**
	 * Returns a description of the error.
	 * @return a description of the error
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
