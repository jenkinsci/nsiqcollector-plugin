package hudson.plugins.nsiq;

import hudson.model.Actionable;
import hudson.model.Build;
import hudson.model.Project;
import hudson.model.ProminentProjectAction;
import hudson.util.Graph;

import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * ProjectAction은 Hudson에서 각 프로젝트의 이름을 클릭했을 때 나타나는 페이지에 관한 action이다.<br/>
 * 주로 그래프 및 간단한 설명을 하는 데 사용된다.
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqProjectAction extends Actionable implements ProminentProjectAction {
	private final Project<?, ?> project;

	public NSiqProjectAction(Project<?, ?> project) {
		this.project = project;
	}

	public Project<?, ?> getProject() {
		return project;
	}

	public Project<?, ?> getOwner() {
		return project;
	}

	/**
	 * 플러그인의 이름을 리턴한다.
	 */
	public String getDisplayName() {
		return Constant.DISPLAY_NAME;
	}

	/**
	 * 아이콘의 이름을 리턴한다.
	 */
	public String getIconFileName() {
		return Constant.ICON_FILENAME;
	}

	/**
	 * 페이지에서 플러그인의 이름을 클릭했을 때 연결되는 URL을 리턴한다.
	 */
	public String getUrlName() {
		return Constant.URL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.search.SearchItem#getSearchUrl()
	 */
	public String getSearchUrl() {
		return getUrlName();
	}

	public boolean isLocView() {
		return getPublisher().isLocView();
	}

	public boolean isComplexityView() {
		return getPublisher().isComplexityView();
	}

	private NSiqPublisher getPublisher() {
		return (NSiqPublisher) project.getPublisher(NSiqPublisher.DESCRIPTOR);
	}

	/**
	 * 해당 프로젝트의 가장 최근 성공한 빌t드에 대한 {@link NSiqBuildAction} 인스턴스를 리턴한다.
	 */
	public NSiqBuildAction getLastResult() {
		for (Build<?, ?> build = project.getLastSuccessfulBuild(); build != null; build = build.getPreviousNotFailedBuild()) {
			NSiqBuildAction action = build.getAction(NSiqBuildAction.class);

			if (action != null) {
				return action;
			}
		}

		return null;
	}

	public Graph getLocDistGraph() {
		if (getLastResult() != null) {
			return getLastResult().getLocDistGraph();
		}
		return null;		
	}
	/**
	 * 성공한 빌드가 있다면, 최근 성공 빌드를 기준으로 한 LOC 그래프를 표시한다.<br/>
	 * 참고로, 프로젝트 이름이 "abc" 이면, 그래프 URL은 "abc/locGraph" 이다.<br/>
	 */
	public Graph getLocGraph() {
		if (getLastResult() != null) {
			return getLastResult().getLocGraph();
		}
		return null;
	}

	/**
	 * 성공한 빌드가 있다면, 최근 성공 빌드를 기준으로 한 복잡도 그래프를 표시한다.<br/>
	 * 참고로, 프로젝트 이름이 "abc" 이면, 그래프 URL은 "abc/complexityGraph" 이다.<br/>
	 */
	public Graph getComplexityGraph() {
		if (getLastResult() != null) {
			return getLastResult().getComplexityGraph();
		}
		return null;
	}

	/**
	 * 가장 최근에 성공한 빌드 번호를 리턴한다.
	 */
	public Integer getLastResultBuild() {
		for (Build<?, ?> build = project.getLastSuccessfulBuild(); build != null; build = build.getPreviousNotFailedBuild()) {
			NSiqBuildAction action = build.getAction(NSiqBuildAction.class);

			if (action != null) {
				return build.getNumber();
			}
		}

		return null;
	}

	/**
	 * 프로젝트에 대해 빌드한 적이 없다면 nodata.jelly 페이지를 보여주고, 빌드한 적이 있으면, 최근에 성공한 빌드 번호에 대한
	 * 결과 페이지로 연결한다.
	 */
	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		Integer buildNumber = getLastResultBuild();

		if (buildNumber == null) {
			rsp.sendRedirect2("nodata");
		} else {
			rsp.sendRedirect2("../" + buildNumber + "/" + Constant.URL);
		}
	}
}
