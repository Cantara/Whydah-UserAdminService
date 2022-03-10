package net.whydah.admin.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;



public class HttpConnectionHelper {
	
	public static class Response {

		public Response(){}
		private int responseCode;
		private byte[] data = null;
		
		public String getContent() {
			if(getData()!=null) {
				return new String(getData());
			}
			return null;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	}

	//GET
	public static Response get(String url) {
		return connect(url, "GET", null, null, null);
	}
	
	public static Response get(String url, String accessToken) {
		return connect(url, "GET", accessToken, null, null);
	}
	
	//POST
	public static Response post(String url, String accessToken, byte[] data) {
		return connect(url, "POST", accessToken, null, data);
	}
	
	public static Response post(String url, byte[] data) {
		return connect(url, "POST", null, null, data);
	}
	
	//PUT
	public static Response put(String url, String accessToken, byte[] data) {
		return connect(url, "PUT", accessToken, null, data);
	}
	
	public static Response put(String url, byte[] data) {
		return connect(url, "PUT", null, null, data);
	}
	
	//DELETE
	public static Response delete(String url) {
		return connect(url, "DELETE", null, null, null);
	}
	
	public static Response delete(String url, byte[] data) {
		return connect(url, "DELETE", null, null, data);
	}
	
	public static Response delete(String url, String accessToken) {
		return connect(url, "DELETE", accessToken, null, null);
	}
	
	public static Response delete(String url, String accessToken, byte[] data) {
		return connect(url, "DELETE", accessToken, null, data);
	}
	
	
	
	public static Response connect(String url, String method, String accessToken, Map<String, String> params, byte[] sentData) {		
		Response responseLine = null; 
		try {
			
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("Accept", "application/json");
			if(accessToken!=null && accessToken.length()>0) {
				conn.setRequestProperty("Authorization",  "Bearer " + accessToken);
			}
			
			if(params!=null) {
				for(String key : params.keySet()) {
					conn.setRequestProperty(key, params.get(key));
				}
			}
			conn.setRequestMethod(method);
			
			
			if(sentData!=null) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				OutputStream os = conn.getOutputStream();
				os.write(sentData);
				os.flush();
				os.close();
			}


			responseLine = readInput(conn);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseLine;
	}

	protected static Response readInput(HttpURLConnection conn) throws UnsupportedEncodingException, IOException {
//		String responseLine;
//		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
//		
//		StringBuilder response = new StringBuilder();
//
//		while ((responseLine = br.readLine()) != null) {
//			response.append(responseLine.trim());
//		}
//
//		br.close();
//		return response.toString();
		Response res = new Response();
		res.setResponseCode(conn.getResponseCode());
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = conn.getInputStream().read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			byte[] byteArray = buffer.toByteArray();
			res.setData(byteArray);//.readAllBytes() is Java9+);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

}
