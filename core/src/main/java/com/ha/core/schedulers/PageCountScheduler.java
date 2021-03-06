package com.ha.core.schedulers;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
			label= "Holland - Page Count Scheduler Service",
			description= "This scheduled service is developed for adding a count to the specified page.",
			immediate=true,
			metatype=true
)
@Service(value = Runnable.class)
public class PageCountScheduler implements Runnable {

	@Property(
				name="scheduler.period",
				label="Interval defining when this Scheduled Service will run",
				description="Enter a value in seconds",
				longValue=10
	)
	
	private static final String PROP_SCHEDULER_PERIOD = "scheduler.period";
	
	@Property(
				name="scheduler.contentpath",
				label="Page Path in AEM",
				description="Please provide the path for the desired page",
				value="/content/ha-base-page"
	)
	
	private static final String PROP_SCHEDULER_CONTENTPATH = "scheduler.contentpath";
	
	private static final long DEFAULT_PROP_SCHEDULER_PERIOD = 10;
	
	private static final String DEFAULT_PROP_SCHEDULER_CONTENTPATH = "/content/ha-base-page";
	
	private ResourceResolver resourceResolver;
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	long schedulerPeriod;
	
	String contentPath;
	
	private final Logger log = LoggerFactory.getLogger(PageCountScheduler.class);
	
	@Activate
	protected void activate(ComponentContext componentContext) {
		Dictionary<?,?> config = componentContext.getProperties();
		this.schedulerPeriod = PropertiesUtil.toLong(config.get(PROP_SCHEDULER_PERIOD), DEFAULT_PROP_SCHEDULER_PERIOD);
		this.contentPath = PropertiesUtil.toString(config.get(PROP_SCHEDULER_CONTENTPATH), DEFAULT_PROP_SCHEDULER_CONTENTPATH);
	}
	
	@Override
	public void run() {
		try {
			resourceResolver = getServiceResourceResolver(resourceResolverFactory);
			Session session = resourceResolver.adaptTo(Session.class);
			Node pageNode = session.getNode(contentPath);
			Node jcrNode = pageNode.getNode("jcr:content");
			if (!jcrNode.hasProperty("scheduledCount")) {
				jcrNode.setProperty("scheduledCount", 1);
			} else {
				long currentCount = jcrNode.getProperty("scheduledCount").getLong();
				jcrNode.setProperty("scheduledCount", currentCount + 1);
			}
			session.save();
		} catch (Exception e) {
			
		}
	}
	
	private ResourceResolver getServiceResourceResolver(ResourceResolverFactory resourceResolverFactory) throws LoginException {
		ResourceResolver serviceResolver = null;
		final Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, (Object) "adminResourceResolver");
		serviceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo);
		return serviceResolver;
	}
	
}