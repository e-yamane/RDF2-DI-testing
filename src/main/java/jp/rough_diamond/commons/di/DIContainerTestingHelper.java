/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.commons.di;

import java.util.*;

/**
 * DIContainerにアクセスするテストを補助するためのヘルパークラス
 * ScalaのSpecはテストがconcurrentで実行されるためDIContainerの内容によって変化する
 * 振る舞いをテストするようなケースでは不具合が乗じてしまう（別のテストによってDIContainerの値が書きかえられる可能性があるため）
 * 本クラスを用いることによりDIContainerの差し替えを抑制しDIContainerの振る舞いをスレッド別に変更できるようになる
 */
public class DIContainerTestingHelper {
    static DIContainer orgDI;

    /**
     * 初期化メソッド
     * 本クラスを使用する場合は必ず呼び出すこと
     */
    public synchronized static void init() {
        try {
            DIContainer di = DIContainerFactory.getDIContainer();
            if(instance == di) {
                return;
            }
            orgDI = di;
        } catch(Exception e) {
            orgDI = null;
        }
        DIContainerFactory.setDIContainer(instance);
    }

    /**
     * BaseとなるDIContainerを差し替えたい場合に使用する
     * 基本あまり使用しない方が良い
     * @param di
     */
    public synchronized static void replaceDI(DIContainer di) {
        orgDI = di;
    }

    /**
     * DIContainer#getObject(Class<T>, Object)呼び出しをHookするリスナを登録する
     * このリスナはそのスレッド内部のみ有効なリスナであり他のスレッドには影響を与えない
     * @param listener
     */
    public static void attach(DIHook listener) {
        listeners.get().add(listener);
    }

    /**
     * DIContainer#getObject(Class<T>, Object)呼び出しをHookするリスナを抹消する
     * @param listener
     */
    public static void detach(DIHook listener) {
        listeners.get().remove(listener);
    }

    private static Iterable<DIHook> iterable() {
        List<DIHook> ret = new ArrayList<DIHook>(listeners.get());
        Collections.reverse(ret);
        return ret;
    }

    private static ThreadLocal<Set<DIHook>> listeners = new ThreadLocal<Set<DIHook>>() {
         @Override
        protected Set<DIHook> initialValue() {
             return new LinkedHashSet<DIHook>();
         }
    };

    /**
     * 特定スレッドのみDIContainerからの返却値を変更したい場合に利用するHookインタフェース
     */
    public static interface DIHook {
        /**
         * キー/タイプに基づくオブジェクトを返却する
         * 返却値がnullの場合は別のHookリスナへ処理を委譲し、全てのリスナがnullを返す場合のみ元となるDIContainerへ問い合わせる
         * @param type
         * @param key
         * @param <T>
         * @return
         */
        public <T> T getObject(Class<T> type, Object key);
    }

    private final static DIContainerExt instance = new DIContainerExt();

    private static class DIContainerExt extends AbstractDIContainer {
        @Override
        public <T> T getObject(Class<T> type, Object key) {
            for(DIHook listener : iterable()) {
                T ret = listener.getObject(type, key);
                if(ret != null) {
                    return ret;
                }
            }
            return orgDI.getObject(type, key);
        }

        @Override
        public <T> T getSource(Class<T> type) {
            return orgDI.getSource(type);
        }
    }
}
