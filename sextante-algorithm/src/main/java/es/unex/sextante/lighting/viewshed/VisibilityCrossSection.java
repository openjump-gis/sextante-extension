/*******************************************************************************
VisibilityCrossSection.java
Copyright (C) Aviad Segev, 2010

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/

package es.unex.sextante.lighting.viewshed;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represent a slice of the Viewshed between two line of sights
 *
 */
public class VisibilityCrossSection {

   private final List<Coordinate> m_rangeOfSight1;
   private final List<Coordinate> m_rangeOfSight2;
   private final double           m_angle1;
   private final double           m_angle2;


   public VisibilityCrossSection(final List<Coordinate> rangeOfSight1,
                                 final List<Coordinate> rangeOfSight2,
                                 final double angle1,
                                 final double angle2) {

      m_rangeOfSight1 = rangeOfSight1;
      m_rangeOfSight2 = rangeOfSight2;
      m_angle1 = angle1;
      m_angle2 = angle2;
   }


   public List<Coordinate> getRangeOfSight1() {
      return m_rangeOfSight1;
   }


   public List<Coordinate> getRangeOfSight2() {
      return m_rangeOfSight2;
   }


   public double getAngle1() {
      return m_angle1;
   }


   public double getAngle2() {
      return m_angle2;
   }
}
