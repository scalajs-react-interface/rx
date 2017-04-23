package sri.rx

import rx._
import sri.core._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName, ScalaJSDefined}
import scala.util.{Failure, Success}

@ScalaJSDefined
abstract class RxVarComponent[P] extends InternalComponentP[Var[P]] {

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private var obs: Obs = _

  private var rxObservers: js.Array[Obs] = js.Array()

  @inline
  @JSName("propsnow")
  def props = jsProps.scalaProps.now

  def updateProps(p: P) = jsProps.scalaProps() = p

  def render(): ReactElement

  def registerRx(rx: Rx[_]) = {
    val rxObs = rx.triggerLater(forceUpdate())
    rxObservers.push(rxObs)
  }

  override def shouldComponentUpdate(nextProps: JSProps {
    type ScalaProps = Var[P]
  }): Boolean = false

  override def componentDidMount(): Unit = {
    obs = jsProps.scalaProps.triggerLater({
      forceUpdate()
    })
  }

  override def componentWillUnmount(): Unit = {
    obs.kill()
    rxObservers.foreach(_.kill())
  }

}

@ScalaJSDefined
abstract class RxComponent[P] extends InternalComponentP[Rx[P]] {

  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private var obs: Obs = _

  private val rxObservers: js.Array[Obs] = js.Array()

  @inline
  @JSName("propsnow")
  def props = jsProps.scalaProps.now

  def render(): ReactElement

  override def shouldComponentUpdate(nextProps: JSProps {
    type ScalaProps = Rx[P]
  }): Boolean = false

  def registerRx(rx: Rx[_]) = {
    val rxObs = rx.triggerLater(forceUpdate())
    rxObservers.push(rxObs)
  }

  override def componentDidMount(): Unit = {
    obs = jsProps.scalaProps.triggerLater(forceUpdate())
  }

  override def componentWillUnmount(): Unit = {
    obs.kill()
    rxObservers.foreach(_.kill())
  }

}

@ScalaJSDefined
abstract class RxAsyncComponent[P] extends InternalComponentP[Future[P]] {

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  implicit val ctx: Ctx.Owner = Ctx.Owner.safe()

  private var _isLoading = true
  private var _isFailed = false
  private var isMounted = false

  private var rxObservers: Seq[Obs] = Seq()

  private var futureValue: js.UndefOr[P] = js.undefined
  private var futureException: js.UndefOr[Throwable] = js.undefined

  @inline
  @JSName("propsnow")
  def props = futureValue

  def exception = futureException

  def isLoading = _isLoading

  def isFailed = _isFailed

  def render(): ReactElement

  override def componentWillReceiveProps(nextProps: JSProps {
    type ScalaProps = Future[P]
  }): Unit = {
    _isLoading = true
    _isFailed = false
    futureValue = js.undefined
    futureException = js.undefined
    processFuture //TODO probably we should move this from here!
  }

  def registerRx(rx: Rx[_]) = {
    val rxObs = rx.foreach(_ => forceUpdate())
    rxObservers :+= rxObs
  }

  @inline
  def processFuture = {
    jsProps.scalaProps onComplete {
      case Success(v) => {
        futureValue = v
        _isLoading = false
        _isFailed = false
        if (isMounted) forceUpdate()
      }
      case Failure(ex) => {
        println(s"failed with exception : $ex")
        futureException = ex
        _isFailed = true
        _isLoading = false
        if (isMounted) forceUpdate()
      }
    }
  }

  override def componentDidMount(): Unit = {
    isMounted = true
    processFuture
  }

  override def componentWillUnmount(): Unit = {
    isMounted = false
    rxObservers.foreach(_.kill())
  }

}
