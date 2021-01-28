/*
 * Copyright (C) 2020 The Josh Tool Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mumu.android.joshautomation.content;

import com.mumu.android.joshautomation.autojob.SelectableSAPAJob;
import com.mumu.android.joshautomation.scripts.claudia.ClaudiaAutoEventJob;
import com.mumu.android.joshautomation.scripts.epic7.Epic7AutoReplayJob;
import com.mumu.android.joshautomation.scripts.fgo.AutoBoxJob;
import com.mumu.android.joshautomation.scripts.fgo.FGOLoopBattleJob;
import com.mumu.android.joshautomation.scripts.konosuba.KonosubaAutoJob;
import com.mumu.android.joshautomation.scripts.light.LightAutoJob;
import com.mumu.android.joshautomation.scripts.saga.SagaAutoJob;

import java.util.ArrayList;

/*
 * AutoJobClasses
 * This is a class holding the static reference for all AutoJob classes
 * Make it easy to create and initialize into the HeadService who holds all the job instances
 */
public class AutoJobClasses {
    public static ArrayList<Class> autoJobLists = new ArrayList<Class>() {
        {
            /*
             * Add Your Job Here !!  Add Your Job Here !!  Add Your Job Here !!
             * Add Your Job Here !!  Add Your Job Here !!  Add Your Job Here !!
             * Add Your Job Here !!  Add Your Job Here !!  Add Your Job Here !!
             * Add Your Job Here !!  Add Your Job Here !!  Add Your Job Here !!
             * comment out the job you don't want to add to AutoJobHandler
             */
            //add(com.mumu.joshautomation.fgo.LoopBattleJob.class);
            //add(com.mumu.joshautomation.fgo.AutoBattleJob.class);
            //add(com.mumu.joshautomation.fgo.PureBattleJob.class);
            //add(com.mumu.joshautomation.fgo.NewFlushJob.class);
            //add(com.mumu.joshautomation.fgo.TWAutoLoginJob.class);
            //add(com.mumu.joshautomation.fgo.AutoBoxJob.class);
            //add(com.mumu.joshautomation.shinobi.ShinobiLoopBattleJob.class);
            //add(com.mumu.joshautomation.caocao.FlushJob.class);
            //add(com.mumu.joshautomation.caocao.FlushMoneyJob.class);
            //add(com.mumu.joshautomation.ro.ROAutoDrinkJob.class);
            //add(com.mumu.joshautomation.epic7.BattleTreasureReminder.class);
            //add(com.mumu.joshautomation.epic7.LoopBattleJob.class);
            add(Epic7AutoReplayJob.class);
            add(LightAutoJob.class);
            add(KonosubaAutoJob.class);
            add(SelectableSAPAJob.class);
            add(FGOLoopBattleJob.class);
            add(AutoBoxJob.class);
            add(SagaAutoJob.class);
            add(ClaudiaAutoEventJob.class);
        }
    };
}
