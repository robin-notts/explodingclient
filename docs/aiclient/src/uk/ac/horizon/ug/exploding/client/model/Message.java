
package uk.ac.horizon.ug.exploding.client.model;
/** 
 * Identity element is ID.
 * 
 * Autogenerated by bean2java.xsl */
public class Message
{

  /** no-arg cons */
  public Message()
  {
  }

  /* implements ., i.e. 
    uk.ac.horizon.ug.exploding.db.Message */
  
  /** internal value - 
   * max length = 20.
  
   */
  protected java.lang.String _ID;
  
  /** getter - 
   * max length = 20.
  
   */
  public java.lang.String getID()
  {
  
    return _ID;
    
  }

  /** setter - 
   * max length = 20.
  
   */
  public void setID(java.lang.String ID)
  {
  
    this._ID = ID;
    
  }

  /** is set?
   */
  public boolean isSetID() {
    return this._ID != null; 
  }

  /** unset
   */
  public void unsetID()  {
    this._ID = null; 
  }

  /** internal value - 
   * max length = 20.
  
   */
  protected java.lang.String _playerID;
  
  /** getter - 
   * max length = 20.
  
   */
  public java.lang.String getPlayerID()
  {
  
    return _playerID;
    
  }

  /** setter - 
   * max length = 20.
  
   */
  public void setPlayerID(java.lang.String playerID)
  {
  
    this._playerID = playerID;
    
  }

  /** is set?
   */
  public boolean isSetPlayerID() {
    return this._playerID != null; 
  }

  /** unset
   */
  public void unsetPlayerID()  {
    this._playerID = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _type;
  
  /** getter - 
   */
  public java.lang.String getType()
  {
  
    return _type;
    
  }

  /** setter - 
   */
  public void setType(java.lang.String type)
  {
  
    this._type = type;
    
  }

  /** is set?
   */
  public boolean isSetType() {
    return this._type != null; 
  }

  /** unset
   */
  public void unsetType()  {
    this._type = null; 
  }

  /** internal value - 
   */
  protected java.lang.String _message;
  
  /** getter - 
   */
  public java.lang.String getMessage()
  {
  
    return _message;
    
  }

  /** setter - 
   */
  public void setMessage(java.lang.String message)
  {
  
    this._message = message;
    
  }

  /** is set?
   */
  public boolean isSetMessage() {
    return this._message != null; 
  }

  /** unset
   */
  public void unsetMessage()  {
    this._message = null; 
  }

  /** internal value - 
   */
  protected java.lang.Long _createTime;
  
  /** getter - 
   */
  public java.lang.Long getCreateTime()
  {
  
    return _createTime;
    
  }

  /** setter - 
   */
  public void setCreateTime(java.lang.Long createTime)
  {
  
    this._createTime = createTime;
    
  }

  /** is set?
   */
  public boolean isSetCreateTime() {
    return this._createTime != null; 
  }

  /** unset
   */
  public void unsetCreateTime()  {
    this._createTime = null; 
  }

  /** internal value - 
   */
  protected java.lang.Boolean _handled;
  
  /** getter - 
   */
  public java.lang.Boolean getHandled()
  {
  
    return _handled;
    
  }

  /** setter - 
   */
  public void setHandled(java.lang.Boolean handled)
  {
  
    this._handled = handled;
    
  }

  /** is set?
   */
  public boolean isSetHandled() {
    return this._handled != null; 
  }

  /** unset
   */
  public void unsetHandled()  {
    this._handled = null; 
  }

  /** internal value - 
   */
  protected java.lang.Long _handledTime;
  
  /** getter - 
   */
  public java.lang.Long getHandledTime()
  {
  
    return _handledTime;
    
  }

  /** setter - 
   */
  public void setHandledTime(java.lang.Long handledTime)
  {
  
    this._handledTime = handledTime;
    
  }

  /** is set?
   */
  public boolean isSetHandledTime() {
    return this._handledTime != null; 
  }

  /** unset
   */
  public void unsetHandledTime()  {
    this._handledTime = null; 
  }

  /** equals */
  public boolean equals(Object o) {
    if (o==null) return false;
    if (!(o instanceof Message)) return false;
    Message oo = (Message)o;
      if (_ID!=oo._ID &&
        (_ID==null || oo._ID==null ||
         !_ID.equals(oo._ID)))
      return false;
    if (_playerID!=oo._playerID &&
        (_playerID==null || oo._playerID==null ||
         !_playerID.equals(oo._playerID)))
      return false;
    if (_type!=oo._type &&
        (_type==null || oo._type==null ||
         !_type.equals(oo._type)))
      return false;
    if (_message!=oo._message &&
        (_message==null || oo._message==null ||
         !_message.equals(oo._message)))
      return false;
    if (_createTime!=oo._createTime &&
        (_createTime==null || oo._createTime==null ||
         !_createTime.equals(oo._createTime)))
      return false;
    if (_handled!=oo._handled &&
        (_handled==null || oo._handled==null ||
         !_handled.equals(oo._handled)))
      return false;
    if (_handledTime!=oo._handledTime &&
        (_handledTime==null || oo._handledTime==null ||
         !_handledTime.equals(oo._handledTime)))
      return false;

    return true;
  }
  /** hashcode */
  public int hashCode() {
    int val = 0;
      if (_ID!=null) val = val ^ _ID.hashCode();
    if (_playerID!=null) val = val ^ _playerID.hashCode();
    if (_type!=null) val = val ^ _type.hashCode();
    if (_message!=null) val = val ^ _message.hashCode();
    if (_createTime!=null) val = val ^ _createTime.hashCode();
    if (_handled!=null) val = val ^ _handled.hashCode();
    if (_handledTime!=null) val = val ^ _handledTime.hashCode();

    return val;
  }
  /** tostring */
  public String toString() {
    StringBuilder str = new StringBuilder("Message:");
    
    str.append("{");
    
    
	str.append("ID=");
	if (_ID!=null) {
	    str.append(_ID.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("playerID=");
	if (_playerID!=null) {
	    str.append(_playerID.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("type=");
	if (_type!=null) {
	    str.append(_type.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("message=");
	if (_message!=null) {
	    str.append(_message.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("createTime=");
	if (_createTime!=null) {
	    str.append(_createTime.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("handled=");
	if (_handled!=null) {
	    str.append(_handled.toString());
	} else {
	    str.append("null");
	}
    str.append(",");
	str.append("handledTime=");
	if (_handledTime!=null) {
	    str.append(_handledTime.toString());
	} else {
	    str.append("null");
	}
    
    str.append("}");

    return str.toString();
  }

}
