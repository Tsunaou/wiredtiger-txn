(defproject jepsen.wiredtiger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/Tsunaou/wiredtiger-txn"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :main jepsen.wiredtiger
  :jvm-opts ["-Djava.awt.headless=true"
             "-Djava.library.path=/usr/local/share/java/wiredtiger-3.3.0"]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [jepsen "0.2.1-SNAPSHOT"]
                 [wiredtiger "3.3.0"]]
  :repl-options {:init-ns jepsen.wiredtiger}
  :plugins [[lein-localrepo "0.5.4"]])
