package tipsystem.tips;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import kr.co.tipos.tips.R;

public class TIPSPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences); // XML에서 설정 정의를 읽고 이를 현재 액티비티의 뷰로 팽창시킴.
		
	}
}