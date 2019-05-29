package com.owm.component.register.transform


import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import org.gradle.api.Project

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class ComponentRegisterAppTransform extends BaseComponentRegisterTransform {

    ComponentRegisterAppTransform(Project project) {
        super(project)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

}
