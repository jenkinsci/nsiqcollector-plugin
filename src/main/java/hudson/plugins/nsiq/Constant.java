package hudson.plugins.nsiq;

import java.util.Locale;

/**
 * Constans
 * @author iceize at NHN Corporation
 * @author JunHo Yoon at NHN Corporation
 * @version $Rev$, $Date$
 */
public final class Constant {
	/**
	 * N'SIQ Collector의 결과 페이지에서 사용하는 URL<br/>
	 * global 및 project 설정에서 form의 parameter 이름의 prefix로 사용함
	 */
	public static final String URL = "nsiq";

	/**
	 * LOC 결과 파일 이름
	 */
	public static final String LOC_FILE = "loc.csv";

	public static final int GRAPH_HISTORY_COUNT = 5;
	/**
	 * Complexity 결과 파일 이름
	 */
	public static final String COMPLEXITY_FILE = "complexity.csv";

	/**
	 * Hudson Plugin의 이름
	 */
	public static final String DISPLAY_NAME = "N'SIQ Collector";

	/**
	 * Hudson Plugin의 아이콘 파일 이름
	 */
	public static final String ICON_FILENAME = "graph.gif";

	/**
	 * Over Complexity 목록의 개수
	 */
	public static final int OVER_COUNT = 10;

	/**
	 * 로케일
	 */
	public static final Locale LOCALE = Locale.KOREAN;

	/**
	 * 결과 저장 파일 이름
	 */
	public static final String RESULT_FILENAME = "nsiqResult.xml";
	
	public static final int MAXIMUM_SOURCE_LINE = 10000;

	public static final boolean DISABLE_IMAGE_CACHE = true;
}
