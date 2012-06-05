import jp.rough_diamond.commons.di.DIContainerTestingHelper._
import jp.rough_diamond.commons.di.{DIContainerTestingHelper, DIContainerFactory, AbstractDIContainer}
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Created with IntelliJ IDEA.
 * User: e-yamane
 * Date: 12/06/05
 * Time: 10:30
 * To change this template use File | Settings | File Templates.
 */

class DIContainerTestingHelperSpec extends FunSpec with ShouldMatchers {
    DIContainerFactory.setDIContainer(DIC)
    init();

    describe("DIContainerTestingHelperの仕様") {
        it("DIHookをattachするとDIContainerの振る舞いを変更できる事")  {
            attach(LISTENER)
            DIContainerFactory.getDIContainer.getObject("hoge") should be ("foo")
        }
        it("DIHookの振る舞いは別のスレッドには影響を与えない事") {
            attach(LISTENER)
            val t = new Thread(RUNNABLE)
            t.start()
            t.join()
            RUNNABLE.value should be ("poge")
        }
        it("DIHookに記載が無いキーに関してはDIContainerの値が返却される事") {
            attach(LISTENER)
            DIContainerFactory.getDIContainer.getObject("abc") should be ("xyz")
        }
        it("DIHookをdetachするとDIHookの影響は受けない事") {
            attach(LISTENER)
            detach(LISTENER)
            DIContainerFactory.getDIContainer.getObject("hoge") should be ("poge")
        }
        it("DIHookを複数attachした場合後にattachしたものが優先される事") {
            attach(LISTENER)
            val additionalListener = new DIHook {
                def getObject[T](`type`: Class[T], key: Any): T = {
                    if (key == "hoge") {
                        "bar".asInstanceOf[T]
                    } else {
                        null.asInstanceOf[T]
                    }
                }
            }
            attach(additionalListener)
            DIContainerFactory.getDIContainer.getObject("hoge") should be ("bar")
        }
    }

    object RUNNABLE extends Runnable {
        var value : String = ""
        @Override
        def run() {
            value = DIContainerFactory.getDIContainer.getObject("hoge").asInstanceOf[String]
        }
    }

    object LISTENER extends DIHook {
        @Override
        def getObject[T](`type`: Class[T], key: AnyRef): T = {
            if (key == "hoge") {
                "foo".asInstanceOf[T]
            } else {
                null.asInstanceOf[T]
            }
        }
    }

    object DIC extends AbstractDIContainer {
            def getObject[T](cl:Class[T], key:Object) : T =  {
            if(key == "hoge") {
                "poge".asInstanceOf[T]
            } else if (key == "abc") {
                "xyz".asInstanceOf[T]
            } else {
                null.asInstanceOf[T]
            }
        }
        def getSource[T](cl:Class[T]) : T = null.asInstanceOf[T]
    }
}
