package controllers

import javax.inject.Inject
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

final class IndexController @Inject()(
    controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {
  def index: Action[AnyContent] = Action {
    Ok("Welcome to secure Play!")
  }
}
