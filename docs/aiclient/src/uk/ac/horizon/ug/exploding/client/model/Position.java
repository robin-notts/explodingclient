
package uk.ac.horizon.ug.exploding.client.model;
/** 
 * Autogenerated by bean2java.xsl */
public class Position
{

  /** no-arg cons */
  public Position()
  {
  }

  /* implements ., i.e. 
    uk.ac.horizon.ug.exploding.db.Position */
  
  /** internal value - 
   */
  protected java.lang.Double _latitude;
  
  /** getter - 
   */
  public java.lang.Double getLatitude()
  {
  
    return _latitude;
    
  }

  /** setter - 
   */
  public void setLatitude(java.lang.Double latitude)
  {
  
    this._latitude = latitude;
    
  }

  /** is set?
   */
  public boolean isSetLatitude() {
    return this._latitude != null; 
  }

  /** unset
   */
  public void unsetLatitude()  {
    this._latitude = null; 
  }

  /** internal value - 
   */
  protected java.lang.Double _longitude;
  
  /** getter - 
   */
  public java.lang.Double getLongitude()
  {
  
    return _longitude;
    
  }

  /** setter - 
   */
  public void setLongitude(java.lang.Double longitude)
  {
  
    this._longitude = longitude;
    
  }

  /** is set?
   */
  public boolean isSetLongitude() {
    return this._longitude != null; 
  }

  /** unset
   */
  public void unsetLongitude()  {
    this._longitude = null; 
  }

  /** internal value - 
   */
  protected java.lang.Double _elevation;
  
  /** getter - 
   */
  public java.lang.Double getElevation()
  {
  
    return _elevation;
    
  }

  /** setter - 
   */
  public void setElevation(java.lang.Double elevation)
  {
  
    this._elevation = elevation;
    
  }

  /** is set?
   */
  public boolean isSetElevation() {
    return this._elevation != null; 
  }

  /** unset
   */
  public void unsetElevation()  {
    this._elevation = null; 
  }

  /** equals */
  public boolean equals(Object o) {
    if (o==null) return false;
    if (!(o instanceof Position)) return false;
    Position oo = (Position)o;
      if (_latitude!=oo._latitude &&
        (_latitude==null || oo._latitude==null ||
         !_latitude.equals(oo._latitude)))
      return false;
    if (_longitude!=oo._longitude &&
        (_longitude==null || oo._longitude==null ||
         !_longitude.equals(oo._longitude)))
      return false;
    if (_elevation!=oo._elevation &&
        (_elevation==null || oo._elevation==null ||
         !_elevation.equals(oo._elevation)))
      return false;

    return true;
  }
  /** hashcode */
  public int hashCode() {
    int val = 0;
      if (_latitude!=null) val = val ^ _latitude.hashCode();
    if (_longitude!=null) val = val ^ _longitude.hashCode();
    if (_elevation!=null) val = val ^ _elevation.hashCode();

    return val;
  }
  /** tostring */
  public String toString() {
    StringBuilder str = new StringBuilder("Position:");
    
    str.append("{");
    
    
	str.append("latitude=");
	if (_latitude!=null) {
	    str.append(_latitude.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("longitude=");
	if (_longitude!=null) {
	    str.append(_longitude.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("elevation=");
	if (_elevation!=null) {
	    str.append(_elevation.toString());
	} else {
	    str.append("null");
	}
    
    str.append("}");

    return str.toString();
  }

}
