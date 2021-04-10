package controllers

import javax.inject.Inject
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

final class IndexController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {
  def index: Action[AnyContent] = Action {
    Ok("Welcome to secure Play!")
  }
}
