package com.owm.component.register.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

/**
 * Created by "ouweiming" on 2019/4/28.
 */
class ComponentRegisterLibTransform extends BaseComponentRegisterTransform {

    ComponentRegisterLibTransform(Project project) {
        super(project)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

}
