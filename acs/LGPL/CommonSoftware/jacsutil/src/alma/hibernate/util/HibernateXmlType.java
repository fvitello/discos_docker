/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) ESO - European Southern Observatory, 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/

package alma.hibernate.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.mchange.v2.c3p0.impl.C3P0ResultSetPeeker;

/**
 * This is a copy of the HibernateXmlType class from jacsutil adapted for Hibernate 5.
 * 
 * A hibernate UserType that supports the Oracle XMLTYPE.
 * 
 * <p>
 * To use this class in your domain class add the following above the class
 * declaration (where the &#064;Entity is): <br>
 * &#064;TypeDef(name = "xmltype", typeClass = HibernateXmlType.class)
 * <p>
 * Then add this to the Java Strings that map Oracle XMLTYPEs in the DB: <br>
 * &#064;Type(type = "xmltype")
 * <p>
 * NOTE(rtobar): When we integrated Robert's class into ACS, we modified it to use reflection so that 
 * we get no errors if the Oracle jars are not on the classpath. Reflection is also used for inspecting
 * for C3P0 classes. If classes are not found, then simply the old CLOB behavior will be used.
 * 
 * @author rkurowsk, 11/10/2018
 */
public class HibernateXmlType implements UserType, Serializable {

	private static final long serialVersionUID = -3359971984030700151L;
	
	private static final String COM_MCHANGE_V2_C3P0 = "com.mchange.v2.c3p0";
	private static final String ORACLE_JDBC = "oracle.jdbc";
	private static final String GET_STRING_VAL = "getStringVal";
	private static final String XML_TYPE = "oracle.xdb.XMLType";
	
	private static final int[] SQL_TYPES = new int[] { 2007 };
	
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor si, Object owner) throws HibernateException, SQLException {

		if( rs.getClass().getName().startsWith(COM_MCHANGE_V2_C3P0) ) {
			rs = C3P0ResultSetPeeker.getInnerFrom(rs);
		}
		
		if ( rs.getClass().getName().startsWith(ORACLE_JDBC) ) {

			// result set comes from oracle thin jdbc driver, and thanks to the hibernate annotations
			// we know that the column type is XMLTYPE, so we can simply cast...
			
			Object xmlType = null;
			try {
				Class<?> clazz = Class.forName(XML_TYPE);
				Method m = clazz.getMethod(GET_STRING_VAL);

				xmlType = rs.getObject(names[0]);
				return (xmlType != null) ? m.invoke(xmlType) : null;
			} catch (Exception e) {
				throw new SQLException("Failed to convert XMLTYPE String to Document for retrieval", e);
			}finally {
				close(xmlType);
			}

		} else {
			return nonOracleNullSafeGet(rs, names, si, owner);
		}
		
	}
	
	private void close(Object xmlType) throws SQLException  {
		
		if(xmlType != null) {
			try {
				Class<?> clazz = Class.forName(XML_TYPE);
				Method m = clazz.getMethod("close");
				m.invoke(xmlType);
			}catch(Exception e) {
				throw new SQLException("Error while closing XMLTYPE", e);
			}
		}
		
	}
	
	private Object nonOracleNullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
		
		Reader reader = rs.getCharacterStream( names[0] );
		if ( reader == null ) return null;
		StringBuilder result = new StringBuilder( 4096 );
		try {
			char[] charbuf = new char[4096];
			for ( int i = reader.read( charbuf ); i > 0 ; i = reader.read( charbuf ) ) {
				result.append( charbuf, 0, i );
			}
		}
		catch (IOException e) {
			throw new SQLException( e.getMessage() );
		}
		
		return result.toString();
	}
	
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor si) throws HibernateException, SQLException {
		
		JdbcNativeExtractor extractor = new JdbcNativeExtractor();
    	Connection connection = extractor.getNativeConnection(st.getConnection());

    	if ( connection.getClass().getName().startsWith(ORACLE_JDBC) ) {
    		Object xmlType = null;
	        try {
	            if (value != null) {
	            	Class<?> clazz = Class.forName(XML_TYPE); 
	            	Constructor<?> con = clazz.getConstructor(Connection.class, String.class);
	            	xmlType = con.newInstance(connection, (String)value);
	            	st.setObject(index, xmlType);
	            } else {
	            	/* TODO: 2007 is oracle.jdbc.OracleTypes.OPAQUE, use reflection */
	                st.setNull( index, 2007, "SYS.XMLTYPE");
	            }
	        }catch (Exception e) {
	            throw new SQLException("Failed to convert Document to XMLTYPE String for storage", e);
	        }finally {
	        	close(xmlType);
	        }

        } else {
        	nonOracleNullSafeSet(st, value, index, si);
        }
	}
	
	private void nonOracleNullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
		
		if ( value != null ) {
			String string = (String) value;
			StringReader reader = new StringReader( string );
			st.setCharacterStream( index, reader, string.length() );
		}
		else {
			st.setNull( index, sqlTypes()[0] );
		}
		
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass() {
		return String.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null && y == null) {
			return true;
		}else if (x == null && y != null) {
			return false;
		}else {
			return x.equals(y);
		}
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
	   if (value == null) {
	        return null;
	    } else {
	        return value;
	    }
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
	    try {
	        return (Serializable)value;
	    } catch (Exception e) {
	        throw new HibernateException("Could not disassemble Document to Serializable", e);
	    }
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
	    try {
	        return (String)cached;
	    } catch (Exception e) {
	        throw new HibernateException("Could not assemble String to Document", e);
	    }
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	
}
