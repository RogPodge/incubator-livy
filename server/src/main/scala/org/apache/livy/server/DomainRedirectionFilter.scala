package org.apache.livy.server

import java.io.IOException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.livy.{LivyConf, Logging}

class DomainRedirectionFilter(HAService: CuratorElectorService) extends Filter
  with Logging
{

  val METHODS_TO_IGNORE = Set("GET", "OPTIONS", "HEAD");

  val HEADER_NAME = "X-Requested-By";

  def isLeader(): Boolean = {
    HAService.currentState == HAState.Active
  }

  override def init(filterConfig: FilterConfig): Unit = {}

  override def doFilter(request: ServletRequest,
                        response: ServletResponse,
                        chain: FilterChain): Unit = {
    info("active leader is:" + HAService.getActiveEndpoint())
    info("current id:" + HAService.getCurrentId())
    if (!isLeader()) {
        val httpRequest = request.asInstanceOf[HttpServletRequest];
	val requestURL = httpRequest.getRequestURL().toString();
	val url = new URL(requestURL);

	val httpServletResponse = response.asInstanceOf[HttpServletResponse];
	httpServletResponse.sendRedirect(HAService.getActiveEndpoint());
    } else {
      chain.doFilter(request, response);
    }
  }
    
  override def destroy(): Unit = {}
}