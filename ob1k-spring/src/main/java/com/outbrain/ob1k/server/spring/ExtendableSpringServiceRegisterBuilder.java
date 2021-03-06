package com.outbrain.ob1k.server.spring;

import com.outbrain.ob1k.Service;
import com.outbrain.ob1k.common.filters.ServiceFilter;
import com.outbrain.ob1k.server.builder.ServerBuilderState;
import com.outbrain.ob1k.server.builder.ServiceRegisterBuilder;
import com.outbrain.ob1k.server.spring.SpringServiceBindBuilder.SpringServiceBindBuilderSection;

import java.util.ArrayList;
import java.util.List;

public class ExtendableSpringServiceRegisterBuilder<B extends ExtendableSpringServiceRegisterBuilder<B>>
        extends ServiceRegisterBuilder<B> {

  private static final NoOpBindSection NO_OP = new NoOpBindSection();
  private final SpringServiceBindBuilder bindBuilder;
  private final SpringBeanContext ctx;

  protected ExtendableSpringServiceRegisterBuilder(final ServerBuilderState state, final SpringBeanContext ctx) {
    super(state);
    this.ctx = ctx;
    this.bindBuilder = new SpringServiceBindBuilder(state, ctx);
  }

  @SafeVarargs
  public final B register(final String ctxName, final Class<? extends Service> serviceType,
                                                               final String path, final Class<? extends ServiceFilter>... filterTypes) {
    return register(ctxName, serviceType, path, NO_OP, filterTypes);
  }

  @SafeVarargs
  public final B register(final String ctxName, final Class<? extends Service> serviceType,
                                                               final String path, final SpringServiceBindBuilderSection bindSection,
                                                               final Class<? extends ServiceFilter>... filterTypes) {
    final List<ServiceFilter> filters = new ArrayList<>();
    if (filterTypes != null) {
      for (final Class<? extends ServiceFilter> filterType : filterTypes) {
        final ServiceFilter filter = ctx.getBean(ctxName, filterType);
        filters.add(filter);
      }
    }
    final Service service = ctx.getBean(ctxName, serviceType);
    register(service, path, filters.toArray(new ServiceFilter[filters.size()]));
    bindSection.apply(bindBuilder);
    return self();
  }

  private static class NoOpBindSection implements SpringServiceBindBuilderSection {

    @Override
    public void apply(final SpringServiceBindBuilder builder) {
      // do nothing
    }
  }
}
