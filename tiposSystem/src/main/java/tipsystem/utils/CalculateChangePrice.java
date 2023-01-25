package tipsystem.utils;

import java.util.HashMap;

public class CalculateChangePrice {
		
		// 이익률변경시 계산기	
		public HashMap<String, String> calculateSellPriByProfitRate (HashMap<String, String> m_temp) {		
			
			String ratio = m_temp.get("Profit_Rate"); //이익률
	    	String Pur_Pri = m_temp.get("Pur_Pri"); //매입가
	    	
	    	if( !ratio.equals("")&& !ratio.equals("0") && !Pur_Pri.equals("0") && !Pur_Pri.equals("") ){
	    		float f_ratio =  Float.valueOf(ratio).floatValue()/100; // 퍼센트값 구함
	    		float f_purchasePrice =  Float.parseFloat(Pur_Pri); //매입가 float casting
	    		float f_salesPrice = f_purchasePrice/(1 - f_ratio); //판매가 구하기 
	    		
	    		// 판매가 반올림 수행
	    		int rest = ((int)(f_salesPrice /10.0f))*10;
	    		int one = (int)f_salesPrice - rest;
	    		if (one>=5) rest+=10;
	    		
	    		//반올림 수행해서 역으로 이익률 계산
	    		f_ratio = (rest - f_purchasePrice) / rest;    		
	    		
	    		m_temp.put("Profit_Rate", String.format("%.2f", f_ratio*100)); // 이익률    		
	    		m_temp.put("Sell_Pri", String.format("%d", rest)); // 판매가	    		
	    	}
	    	return m_temp;
		}
		
		//판매가 계산시
		public HashMap<String, String> calculateSellPriBySellPrice (HashMap<String, String> m_temp){
			String salesPrice = m_temp.get("Sell_Pri");
	    	String Pur_Pri = m_temp.get("Pur_Pri");

	    	if(!salesPrice.equals("") && !salesPrice.equals("0")) {
	    		float f_salesPrice =  Float.parseFloat(salesPrice);
	    		
	    		// 반올림 수행
	    		int rest = ((int)(f_salesPrice /10.0f))*10;
	    		int one = (int)f_salesPrice - rest;
	    		if (one>=5) rest+=10;
	    		
	    		m_temp.put("Sell_Pri", String.format("%d", rest)); // 판매가
	    		
	    		if(!Pur_Pri.equals("") && !salesPrice.equals("0") && !salesPrice.equals("0.00") ) {
		    		
		    		float f_purchasePrice =  Float.parseFloat(Pur_Pri);
		    		float f_ratio = (rest - f_purchasePrice) / rest;
		    		
		    		m_temp.put("Profit_Rate", String.format("%.2f", f_ratio*100)); // 이익률 
		    	}else{
		    		m_temp.put("Profit_Rate", String.format("%.2f", 100f)); // 이익률
		    	}
	    	}		    		
			return m_temp;			
		}
		
		//매입가 변경시 원가 및 부가세 계산 판매가가 있을시 이익률 계산
		public HashMap<String, String> calculateSellPriByPurprice (HashMap<String, String> m_temp){
			String purchasePrice = m_temp.get("Pur_Pri"); //매입가
			String tax_yn = m_temp.get("Tax_YN"); //면과세
	    	String Sell_Pri = m_temp.get("Sell_Pri"); //판매가
	    	
	    	if(!purchasePrice.equals("") && !purchasePrice.equals("0") && !purchasePrice.equals("0.00")) {
	    		float f_purchasePrice =  Float.parseFloat(purchasePrice);	    		
		    	m_temp.put("Pur_Pri", String.format("%.2f", f_purchasePrice)); // 매입가
		    	if(tax_yn.equals("1")){ //과세
	    		m_temp.put("Pur_Cost", String.format("%.2f", f_purchasePrice/1.1)); // 매입원가
	    		m_temp.put("Add_Tax", String.format("%.2f", f_purchasePrice - ( f_purchasePrice/1.1)));//부가세
		    	}
	    		if(tax_yn.equals("0")){ //면세
		    		m_temp.put("Pur_Cost", String.format("%.2f", f_purchasePrice)); // 매입원가
		    		m_temp.put("Add_Tax", String.format("%.2f", 0f));//부가세
		    	}
	    	}
	    	
	    	if(!Sell_Pri.equals("")&& !Sell_Pri.equals("0") && !purchasePrice.equals("") && !purchasePrice.equals("0") && !purchasePrice.equals("0.00")) {
	    		float f_salesPrice =  Float.parseFloat(Sell_Pri);
	    		float f_purchasePrice =  Float.parseFloat(purchasePrice);			    		
	    		float f_ratio = (f_salesPrice - f_purchasePrice) / f_salesPrice ;			    		
	    		m_temp.put("Profit_Rate", String.format("%.2f", f_ratio*100)); // 이익률			    		
	    	} 
	    	
	    	if(( purchasePrice.equals("") || purchasePrice.equals("0") || purchasePrice.equals("0.00")) && ( !Sell_Pri.equals("") && !Sell_Pri.equals("0")) ) {
	    		m_temp.put("Pur_Pri", "0"); // 매입가 
	    		m_temp.put("Pur_Cost", "0"); // 매입원가
	    		m_temp.put("Add_Tax", "0");//부가세
	    		m_temp.put("Profit_Rate", String.format("%.2f", 100f)); // 이익률			 
	    	}
			
			return m_temp;
		}
		
		// 면과세 설정변경시 매입원가 부가세 변경하기
		public HashMap<String, String> tax_YnChooseSelected(HashMap<String, String> m_temp){
			String tax_yn = m_temp.get("Tax_YN");
			String pur_pri = m_temp.get("Pur_Pri");			
			if ( tax_yn.equals("0") && !pur_pri.equals("") && !pur_pri.equals("0") && !pur_pri.equals("0.00")){  //면세 부가세 0원
				m_temp.put("Pur_Cost", pur_pri);
				m_temp.put("Add_Tax", String.format("%.2f", 0f));				
			}
			if( tax_yn.equals("1") && !pur_pri.equals("") && !pur_pri.equals("0") && !pur_pri.equals("0.00") ){
				float f_pur_pri =  Float.parseFloat(pur_pri);
				m_temp.put("Pur_Cost", String.format("%.2f", f_pur_pri/1.1)); // 매입원가
	    		m_temp.put("Add_Tax", String.format("%.2f", f_pur_pri - ( f_pur_pri/1.1)));//부가세
			}			
			return m_temp;
		}
		
		
		
		//2018-02-20 도매단가 변경시 이익률 계산하기
		// 이익률변경시 계산기	
		/***
		 * 도매 이익률 변경시 이익률과 매입가를 기준으로 판매가를 변경합니다.
		 * @param m_temp 상품정보 
		 * @param num A판가, B판가, C판가 : 0,1,2
		 * @return m_temp 상품정보
		 */
		public HashMap<String, String> calculateDomeSellPriByProfitRate (HashMap<String, String> m_temp, int num) {		
						
			String sell_pri = "";
	    	String profit_rate = "";
	    	switch(num){
	    	case 0:
	    		sell_pri = "Sell_APri";
	    		profit_rate = "Profit_Rate_APri";
	    		break;
	    	case 1:
	    		sell_pri = "Sell_BPri";
	    		profit_rate = "Profit_Rate_BPri";
	    		break;
	    	case 2:
	    		sell_pri = "Sell_CPri";
	    		profit_rate = "Profit_Rate_CPri";
	    		break;	    		
	    	default:
	    		return m_temp;
	    	}
	    		    	
	    	String ratio = m_temp.get(profit_rate); //이익률
	    	String Pur_Pri = m_temp.get("Pur_Pri");
			
	    	if( !ratio.equals("")&& !ratio.equals("0") && !Pur_Pri.equals("0") && !Pur_Pri.equals("") ){
	    		float f_ratio =  Float.valueOf(ratio).floatValue()/100; // 퍼센트값 구함
	    		float f_purchasePrice =  Float.parseFloat(Pur_Pri); //매입가 float casting
	    		float f_salesPrice = f_purchasePrice/(1 - f_ratio); //판매가 구하기 
	    		
	    		// 판매가 반올림 수행
	    		int rest = ((int)(f_salesPrice /10.0f))*10;
	    		int one = (int)f_salesPrice - rest;
	    		if (one>=5) rest+=10;
	    		
	    		//반올림 수행해서 역으로 이익률 계산
	    		f_ratio = (rest - f_purchasePrice) / rest;    		
	    		
	    		m_temp.put(profit_rate, String.format("%.2f", f_ratio*100)); // 이익률    		
	    		m_temp.put(sell_pri, String.format("%d", rest)); // 판매가	    		
	    	}
	    	return m_temp;
		}
		
		//판매가 계산시
		/*** 
		 * 도매 판매가변경 시 이익률을 계산합니다.
		 * @param m_temp 상품정보를 입력 받습니다. 
		 * @param num A판가, B판가, C판가 : 0,1,2
		 * @return HashMap<String, String> Profit_Rate_APri
		 */
		public HashMap<String, String> calculateDomeSellPriBySellPrice (HashMap<String, String> m_temp, int num){
			

	    	String sell_pri = "";
	    	String profit_rate = "";
	    	switch(num){
	    	case 0:
	    		sell_pri = "Sell_APri";
	    		profit_rate = "Profit_Rate_APri";
	    		break;
	    	case 1:
	    		sell_pri = "Sell_BPri";
	    		profit_rate = "Profit_Rate_BPri";
	    		break;
	    	case 2:
	    		sell_pri = "Sell_CPri";
	    		profit_rate = "Profit_Rate_CPri";
	    		break;	    		
	    	default:
	    		return m_temp;
	    	}
	    	
	    	String salesPrice = m_temp.get(sell_pri);
	    	String Pur_Pri = m_temp.get("Pur_Pri");
	    	
	    	
	    	if(!salesPrice.equals("") && !salesPrice.equals("0")) {
	    		float f_salesPrice =  Float.parseFloat(salesPrice);
	    		
	    		// 반올림 수행
	    		int rest = ((int)(f_salesPrice /10.0f))*10;
	    		int one = (int)f_salesPrice - rest;
	    		if (one>=5) rest+=10;
	    		
	    		m_temp.put(sell_pri, String.format("%d", rest)); // 판매가
	    		
	    		if(!Pur_Pri.equals("") && !salesPrice.equals("0") && !salesPrice.equals("0.00") ) {
		    		
		    		float f_purchasePrice =  Float.parseFloat(Pur_Pri);
		    		float f_ratio = (rest - f_purchasePrice) / rest;
		    		
		    		m_temp.put(profit_rate, String.format("%.2f", f_ratio*100)); // 이익률 
		    	}else{
		    		m_temp.put(profit_rate, String.format("%.2f", 100f)); // 이익률
		    	}
	    	}else{
	    		m_temp.put(profit_rate, "0"); //매입가가 비어있다면 이익률도 빈칸으로 변경
	    	}
			return m_temp;			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
}
