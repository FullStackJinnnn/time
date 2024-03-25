package infinitystone.chalKag.biz.freePost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository("freePostDAO")
public class FreePostDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	// 게시글 전체 보기 자유게시판, 게시판 이미지, 좋아요 테이블 조인문
	private static final String SELECTALL_FREEPOST = "SELECT " 
			+ "		FREEPOST.FREEPOST_id, " 
			+ " 	FREEPOST.FREEPOST_title, "
			+ " 	FREEPOST.FREEPOST_content, " 
			+ " 	FREEPOST.FREEPOST_date, "
			+ " CASE "
			+ "     WHEN TIMESTAMPDIFF(MINUTE, FREEPOST.FREEPOST_date, NOW()) < 60 THEN CONCAT(TIMESTAMPDIFF(MINUTE, FREEPOST.FREEPOST_date, NOW()), ' 분 전') "
			+ "     WHEN TIMESTAMPDIFF(HOUR, FREEPOST.FREEPOST_date, NOW()) < 24 THEN CONCAT(TIMESTAMPDIFF(HOUR, FREEPOST.FREEPOST_date, NOW()), ' 시간 전') "
			+ "     ELSE CONCAT(TIMESTAMPDIFF(DAY, FREEPOST.FREEPOST_date, NOW()), ' 일 전') "
			+ "    	END AS FREEPOST_date,"
			+ " 	FREEPOST.MEMBER_id, "
			+ " 	MEMBER.MEMBER_nickname, " 
			+ " 	FREEPOST.FREEPOST_viewcnt, "
			+ " 	COUNT(RECOMMEND.POST_id) AS RECOMMEND_cnt, " 
			+ " FROM " 
			+ " 	FREEPOST " 
			+ " LEFT JOIN " 
		    + " 	MEMBER ON FREEPOST.MEMBER_id = MEMBER.MEMBER_id "
			+ " LEFT JOIN "
			+ " 	RECOMMEND ON FREEPOST.FREEPOST_id = RECOMMEND.POST_id "
			+ " GROUP BY " 
			+ " 	FREEPOST.FREEPOST_id, "
			+ "		MEMBER.MEMBER_nickname"
			+ "ORDER BY "
		    + "		FREEPOST_id DESC ";
	
	// 메인페이지 프리미엄 회원글 출력
		private static final String SELECTALL_PREMIUMFREEPOST= "SELECT "
				+ "			    FREEPOST.FREEPOST_title, "
				+ "				   MEMBER.MEMBER_grade ,  "
				+ "			FROM   "
				+ "			    FREEPOST  "
				+ "			LEFT JOIN   "
				+ "			    POSTIMG ON FREEPOST.FREEPOST_id = POSTIMG.POST_id   "
				+ "			INNER JOIN  "
				+ "			    MEMBER ON MEMBER.MEMBER_id = FREEPOST.MEMBER_id "
				+ "			WHERE   "
				+ "			    MEMBER.MEMBER_grade = 'PREMIUM'"
				+ "			ORDER BY "
				+ "    			FREEPOST.FREEPOST_date DESC "
				+ "			LIMIT 2 "; 
	
	// 게시글 상세보기 게시글 전체 보기 자유게시판, 게시판 이미지, 좋아요 테이블 조인문
	private static final String SELECTONE_FREEPOST = "SELECT " 
			+ "	FREEPOST.FREEPOST_id, "
			+ "	FREEPOST.MEMBER_id, " 
			+ " FREEPOST.MEMBER_nickname, "
			+ "	FREEPOST.FREEPOST_title, " 
			+ "	FREEPOST.FREEPOST_content, "
			+ "	FREEPOST.FREEPOST_date, "
			+ " CASE "
			+ "        WHEN TIMESTAMPDIFF(MINUTE, FREEPOST.FREEPOST_date, NOW()) < 60 THEN CONCAT(TIMESTAMPDIFF(MINUTE, FREEPOST.FREEPOST_date, NOW()), ' 분 전') "
			+ "        WHEN TIMESTAMPDIFF(HOUR, FREEPOST.FREEPOST_date, NOW()) < 24 THEN CONCAT(TIMESTAMPDIFF(HOUR, FREEPOST.FREEPOST_date, NOW()), ' 시간 전') "
			+ "        ELSE CONCAT(TIMESTAMPDIFF(DAY, FREEPOST.FREEPOST_date, NOW()), ' 일 전') "
			+ "    END AS FREEPOST_date,"
			+ " FREEPOST.FREEPOST_viewcnt, " 
			+ "	COUNT(RECOMMEND.POST_id) AS RECOMMEND_cnt "
			+ " FROM " 
			+ "		FREEPOST " 
			+ " INNER JOIN "
		    + "		MEMBER "
			+ " LEFT JOIN "
			+ "		RECOMMEND ON FREEPOST.FREEPOST_id = RECOMMEND.POST_id "
			+ " LEFT JOIN "
			+ "    PROFILEIMG ON FREEPOST.MEMBER_id = PROFILEIMG.MEMBER_id "
			+ " WHERE "
			+ "		FREEPOST.FREEPOST_id = ? " 
			+ " GROUP BY " 
			+ "		FREEPOST.FREEPOST_id, "
			+ " 	FREEPOST.MEMBER_nickname, "
			+ " 	PROFILEIMG.PROFILEIMG_name";

	// 자유게시판 글 작성 게시판 이미지와 글 내용 인서트를 따로 받음
	private static final String INSERT = "INSERT INTO FREEPOST(MEMBER_id,FREEPOST_title,FREEPOST_content,FREEPOST_viewcnt) "
			+ "							VALUES(?,?,?,0)";
	private static final String UPDATE = "UPDATE FREEPOST SET FREEPOST_title=?, FREEPOST_content=? "
										+ "	WHERE "
										+ " FREEPOST_id = ? ";
	private static final String UPDATE_VIEWCNT = "UPDATE BOARD SET FREEPOST_viewcnt = (FREEPOST_viewcnt+1) WHERE FREEPOST_id=? ";
	private static final String DELETE = "DELETE FROM FREEPOST WHERE FREEPOST_id = ?";

	public List<FreePostDTO> selectAll(FreePostDTO freePostDTO) {
		List<FreePostDTO> result = null;

		try { // 게시판 전체 출력해주는 행동이라면 selectAll 쿼리문 실행
			if (freePostDTO.getSearchCondition().equals("freePostList")) {
				result = (List<FreePostDTO>) jdbcTemplate.query(SELECTALL_FREEPOST, new FreePostSelectAllRowMapper());
				return result;
			}
			else if(freePostDTO.getSearchCondition().equals("freePostPremiumList")) {
				result = (List<FreePostDTO>) jdbcTemplate.query(SELECTALL_PREMIUMFREEPOST, new FreePostPremiumSellectAllRowMapper());
				System.out.println("FreePostDAO(premiumSelectAll) 로그 = [" + result + "]");
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null; // 실패시 null return
	}

	public FreePostDTO selectOne(FreePostDTO freePostDTO) {
		FreePostDTO result = null;
		// preparestatement 에 셋하는 애들 sql 에 ? 자리에 들어가는애들
		try {											
			if (freePostDTO.getSearchCondition().equals("freePostSingle")) {
				Object[] args = { freePostDTO.getFreePostId() };
				result = jdbcTemplate.queryForObject(SELECTONE_FREEPOST, args, new FreePostSelectOneRowMapper());
				return result;
			}
		} catch (Exception e) {
			return null;
		}
		return null; // 실패시 null return
	}

	public boolean insert(FreePostDTO freePostDTO) {
		int result = 0;
		result = jdbcTemplate.update(INSERT,freePostDTO.getMemberId(),freePostDTO.getFreePostTitle(),freePostDTO.getFreePostContent());
		if (result <= 0) {
			return false;
		}
		return true;
	}

	public boolean update(FreePostDTO freePostDTO) {
		int result = 0;								
		if (freePostDTO.getSearchCondition().equals("freePostViewcntUpdate")) {
			result = jdbcTemplate.update(UPDATE_VIEWCNT,freePostDTO.getFreePostId());
		} else if(freePostDTO.getSearchCondition().equals("freePostUpdate")) {
			result = jdbcTemplate.update(UPDATE,freePostDTO.getFreePostTitle(),freePostDTO.getFreePostContent());
		}
		if (result <= 0) {
			return false;
		}
		return true;
	}

	public boolean delete(FreePostDTO freePostDTO) {
		int result = jdbcTemplate.update(DELETE,freePostDTO.getFreePostId());
		if (result <= 0) {
			return false;
		}
		return true;
	}
}

class FreePostSelectAllRowMapper implements RowMapper<FreePostDTO> {

	@Override
	public FreePostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub

		FreePostDTO data = new FreePostDTO();

		data.setFreePostId(rs.getString("FREEPOST_id"));
		data.setMemberId(rs.getString("MEMBER_id"));
		data.setMemberNickname(rs.getString("MEMBER_nickname"));
		data.setFreePostDate(rs.getString("FREEPOST_date"));
		data.setFreePostTitle(rs.getString("FREEPOST_title"));
		data.setFreePostContent(rs.getString("FREEPOST_content"));
		data.setFreePostViewcnt(rs.getString("FREEPOST_viewcnt"));
		data.setRecommendCnt(rs.getString("RECOMMEND_cnt"));
		return data;
	}

}

class FreePostSelectOneRowMapper implements RowMapper<FreePostDTO> {

	@Override
	public FreePostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub

		FreePostDTO data = new FreePostDTO();

		data.setFreePostId(rs.getString("FREEPOST_id"));
		data.setMemberId(rs.getString("MEMBER_id"));
		data.setMemberNickname(rs.getString("MEMBER_nickname"));
		data.setProfileImgName(rs.getString("PROFILEIMG_name"));
		data.setFreePostDate(rs.getString("FREEPOST_date"));
		data.setFreePostTitle(rs.getString("FREEPOST_title"));
		data.setFreePostContent(rs.getString("FREEPOST_content"));
		data.setFreePostViewcnt(rs.getString("FREEPOST_viewcnt"));
		data.setRecommendCnt(rs.getString("RECOMMEND_cnt"));
		return data;
	}

}

class FreePostPremiumSellectAllRowMapper implements RowMapper<FreePostDTO>{

	@Override
	public FreePostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		FreePostDTO data = new FreePostDTO();

		data.setFreePostId(rs.getString("freePost_id"));
		data.setFreePostTitle(rs.getString("freePost_title"));

		return data;
	}
	
}
